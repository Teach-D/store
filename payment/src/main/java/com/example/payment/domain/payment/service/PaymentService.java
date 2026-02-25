package com.example.payment.domain.payment.service;

import com.example.payment.domain.outbox.OutboxEvent;
import com.example.payment.domain.outbox.OutboxEventRepository;
import com.example.payment.domain.payment.dto.OrderCreatedEvent;
import com.example.payment.domain.payment.dto.PaymentCompletedEvent;
import com.example.payment.domain.payment.dto.PaymentConfirmRequest;
import com.example.payment.domain.payment.dto.PaymentFailedEvent;
import com.example.payment.domain.payment.entity.Payment;
import com.example.payment.domain.payment.repository.PaymentRepository;
import com.example.payment.global.toss.TossPaymentGateway;
import com.example.payment.global.toss.TossPaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final TossPaymentGateway tossPaymentGateway;

    private static final String AGGREGATE_TYPE_PAYMENT = "PAYMENT";

    public void initPayment(OrderCreatedEvent event) {

        boolean alreadyExists = paymentRepository.existsByOrderId(event.getOrderId());
        if (alreadyExists) {
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getTotalPrice())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
    }

    public void confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다. orderId: " + request.getOrderId()));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 결제입니다. status: " + payment.getStatus());
        }

        if (payment.getAmount() != request.getAmount()) {
            log.warn("결제 금액 불일치 orderId: {}, 요청: {}, DB: {}", request.getOrderId(), request.getAmount(), payment.getAmount());
            payment.fail("결제 금액 불일치");
            publishPaymentFailedEvent(payment);
            return;
        }

        try {
            String tossOrderId = "ORDER" + request.getOrderId();
            TossPaymentResponse tossResponse = tossPaymentGateway.confirm(
                    request.getPaymentKey(),
                    tossOrderId,
                    request.getAmount()
            );

            log.info("토스 승인 성공 orderId: {}, paymentKey: {}, status: {}",
                    request.getOrderId(), tossResponse.getPaymentKey(), tossResponse.getStatus());

            payment.complete(tossResponse.getPaymentKey(), tossResponse.getMethod());
            publishPaymentCompletedEvent(payment);

        } catch (feign.FeignException e) {
            log.error("토스 승인 실패 orderId: {}, error: {}", request.getOrderId(), e.contentUTF8());
            payment.fail("PG 승인 실패: " + e.contentUTF8());
            publishPaymentFailedEvent(payment);
            throw e;
        } catch (Exception e) {
            log.error("토스 승인 실패 orderId: {}, error: {}", request.getOrderId(), e.getMessage());
            payment.fail("PG 승인 실패: " + e.getMessage());
            publishPaymentFailedEvent(payment);
            throw new RuntimeException("결제 승인 실패", e);
        }
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        try {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    payment.getOrderId(),
                    payment.getId(),
                    payment.getAmount()
            );
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(
                    AGGREGATE_TYPE_PAYMENT,
                    payment.getId().toString(),
                    "PAYMENT_COMPLETED",
                    payload
            ));
            log.info("결제 완료 이벤트 발행 paymentId: {}, orderId: {}", payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("결제 완료 이벤트 발행 실패 paymentId: {}", payment.getId());
            throw new RuntimeException(e);
        }
    }

    private void publishPaymentFailedEvent(Payment payment) {
        try {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    payment.getOrderId(),
                    payment.getId(),
                    payment.getFailureReason()
            );
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(
                    AGGREGATE_TYPE_PAYMENT,
                    payment.getId().toString(),
                    "PAYMENT_FAILED",
                    payload
            ));
            log.info("결제 실패 이벤트 발행 paymentId: {}, orderId: {}", payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("결제 실패 이벤트 발행 실패 paymentId: {}", payment.getId());
            throw new RuntimeException(e);
        }
    }
}
