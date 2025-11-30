package com.example.payment.domain.payment.consumer;

import com.example.payment.domain.payment.dto.OrderCreatedEvent;
import com.example.payment.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);

        try {
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", event, e);
            throw e;
        }
    }
}
