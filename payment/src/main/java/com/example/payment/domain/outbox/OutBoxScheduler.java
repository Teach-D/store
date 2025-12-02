package com.example.payment.domain.outbox;

import com.example.payment.domain.payment.dto.OrderCreatedEvent;
import com.example.payment.domain.payment.dto.PaymentCompletedEvent;
import com.example.payment.domain.payment.dto.PaymentFailedEvent;
import com.example.payment.global.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class OutBoxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void publishOutboxEvents() {
        try {
            List<OutboxEvent> events = outboxEventRepository.findByPublishedAndEventTypeStartingWithOrderByCreatedAtAsc(false, "PAYMENT");

            if (events.isEmpty()) {
                return;
            }

            log.info("payment outbox event 발생 시작, {}", events.size());

            for (OutboxEvent event : events) {
                try {
                    publishEvent(event);

                    log.info("이벤트 발행 성공: id={}, type={}", event.getId(), event.getEventType());

                    event.changeAsPublished();
                }  catch (Exception e) {
                    log.error("발행 실패, 다음 스케줄에 재시도", e);
                }
            }

        } catch (Exception e) {
            log.error("outbox 스케줄링 실패");
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) throws JsonProcessingException {
        String eventType = outboxEvent.getEventType();
        String payload = outboxEvent.getPayload();

        switch (eventType) {
            case "PAYMENT_COMPLETED":
                PaymentCompletedEvent completedEvent = objectMapper.readValue(
                        payload,
                        PaymentCompletedEvent.class
                );

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PAYMENT_EXCHANGE,
                        "payment.completed",
                        completedEvent
                );
                break;
            case "PAYMENT_FAILED":
                PaymentFailedEvent failedEvent = objectMapper.readValue(
                        payload,
                        PaymentFailedEvent.class
                );

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PAYMENT_EXCHANGE,
                        "payment.failed",
                        failedEvent
                );
                break;

            default:
        }

    }
}
