package com.example.payment.domain.payment.service;

import com.example.payment.domain.outbox.OutboxEvent;
import com.example.payment.domain.outbox.OutboxEventRepository;
import com.example.payment.domain.payment.dto.OrderCreatedEvent;
import com.example.payment.domain.payment.dto.PaymentCompletedEvent;
import com.example.payment.domain.payment.dto.PaymentFailedEvent;
import com.example.payment.domain.payment.entity.Payment;
import com.example.payment.domain.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE_PAYMENT = "PAYMENT";
    private final OutboxEventRepository outboxEventRepository;

    public void processPayment(OrderCreatedEvent event) {
        log.info("결제 처리 시작 orderId : {}",  event.getOrderId());

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getTotalPrice())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        boolean paymentSuccess = simulatePayment(event.getUserId());

        if (paymentSuccess) {
            log.info("결제 처리 성공 : paymentId : {}, orderId : {}", payment.getId(), event.getOrderId());
            payment.complete();
            publishPaymentCompletedEvent(payment);
        } else {
            log.info("결제 처리 실패 : paymentId : {}, orderId : {}", payment.getId(), event.getOrderId());
            payment.fail("이유 : 잔액 부족");
            publishPaymentFailedEvent(payment);
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
            OutboxEvent outboxEvent = new OutboxEvent(
                    AGGREGATE_TYPE_PAYMENT,
                    payment.getId().toString(),
                    "PAYMENT_FAILED",
                    payload
            );

            outboxEventRepository.save(outboxEvent);
            log.info("결제 실패 발행 완료, paymentId : {}, orderId : {}", payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.info("결제 실패 발행 실패, paymentId : {}, orderId : {}", payment.getId(), payment.getOrderId());
            throw new RuntimeException(e);
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
            OutboxEvent outboxEvent = new OutboxEvent(
                    AGGREGATE_TYPE_PAYMENT,
                    payment.getId().toString(),
                    "PAYMENT_COMPLETED",
                    payload
            );

            outboxEventRepository.save(outboxEvent);
            log.info("결제 성공 발행 완료, paymentId : {}, orderId : {}", payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.info("결제 성공 발행 실패, paymentId : {}, orderId : {}", payment.getId(), payment.getOrderId());
            throw new RuntimeException(e);
        }

    }

    private boolean simulatePayment(Long userId) {
        // 테스트용: userId가 짝수면 성공, 홀수면 실패
        return userId % 2 == 0;
    }
}
