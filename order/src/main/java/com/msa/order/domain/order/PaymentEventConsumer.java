package com.msa.order.domain.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.domain.order.dto.OrderCreatedEvent;
import com.msa.order.domain.order.dto.PaymentCompletedEvent;
import com.msa.order.domain.order.dto.PaymentFailedEvent;
import com.msa.order.domain.order.dto.StockRestoreEvent;
import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.repository.OrderRepository;
import com.msa.order.domain.order.service.OrderService;
import com.msa.order.domain.outbox.OutboxEvent;
import com.msa.order.domain.outbox.OutboxEventRepository;
import com.msa.order.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.msa.order.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @RabbitListener(queues = "payment.completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Completed payment 수신 : {}", event);

        try {
            orderService.conformOrder(event.getOrderId());
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
            publishCartDeleteEvent(order);

            log.info("주문 확정: {}", event);
        } catch (Exception e) {
            log.error("Error while processing PaymentCompletedEvent: {}", event, e);
        }
    }

    @RabbitListener(queues = "payment.failed")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Failed payment 수신 : {}", event);

        try {
            orderService.cancelOrder(event.getOrderId());
            Order order = orderRepository.findByOrderIdWithItems(event.getOrderId())
                    .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

            log.info("payment.failed에서 orderId; {}", order.getOrderId());

            publishStockRestoreEvent(order);

            log.info("주문 실패 확정: {}", event);
        } catch (Exception e) {
            log.error("Error while processing PaymentFailedEvent: {}", event, e);
        }
    }

    private void publishCartDeleteEvent(Order order) {
        try {
            Long memberId = order.getMemberId();

            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getOrderId(),
                    order.getMemberId(),
                    null,
                    LocalDateTime.now(),
                    0
            );

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    "ORDER",
                    order.getOrderId().toString(),
                    "CART_DELETE",
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            log.info("cart delete 이벤트 outbox 저장 orderId : {}", order.getOrderId());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void publishStockRestoreEvent(Order order) {
        try {
            StockRestoreEvent event = StockRestoreEvent.of(order);

            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    "ORDER",
                    order.getOrderId().toString(),
                    "STOCK_RESTORE",
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            log.info("재고에 대한 보상 트랜잭션 outbox 저장 orderId : {}", order.getOrderId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
