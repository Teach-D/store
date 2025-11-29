package com.msa.order.domain.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.domain.order.dto.OrderCreatedEvent;
import com.msa.order.global.RabbitMQConfig;
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
            long start = System.currentTimeMillis();
            List<OutboxEvent> events = outboxEventRepository.findByPublishedOrderByCreatedAtAsc(false);

            if (events.isEmpty()) {
                return;
            }

            log.info("outbox event 발생 시작, {}", events.size());

            for (OutboxEvent event : events) {
                try {
                    publishEvent(event);

                    log.info("이벤트 발행 성공: id={}, type={}", event.getId(), event.getEventType());

                    event.changeAsPublished();
                }  catch (Exception e) {
                    log.error("발행 실패, 다음 스케줄에 재시도", e);
                }
            }

            long end = System.currentTimeMillis();
            long elapsed = end - start;
            log.info("스케줄링 한 사이클 소요 시간 : {}", elapsed);
        } catch (Exception e) {
            log.error("outbox 스케줄링 실패");
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) throws JsonProcessingException {
        String eventType = outboxEvent.getEventType();
        String payload = outboxEvent.getPayload();

        switch (eventType) {
            case "ORDER_CREATED":
                OrderCreatedEvent event = objectMapper.readValue(
                        payload,
                        OrderCreatedEvent.class
                );

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        RabbitMQConfig.ORDER_ROUTING_KEY,
                        event
                );
                break;
            default:
                throw new IllegalArgumentException();
        }

    }
}
