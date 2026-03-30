package com.msa.order.domain.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.domain.order.dto.OrderCreatedEvent;
import com.msa.order.domain.order.dto.StockRestoreEvent;
import com.msa.order.global.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Outbox 이벤트 발행 스케줄러
     *
     * 핵심 최적화:
     * 1. @Transactional 제거 → DB 커넥션을 RabbitMQ 네트워크 I/O 중 점유하지 않음
     *    (커넥션 풀 고갈로 인한 p95 급등 방지)
     * 2. Publisher Confirms(CorrelationData) → 브로커 ACK 확인 후에만 published 처리
     *    (네트워크 오류/브로커 재시작 시 메시지 유실 방지, 데이터 정합성 확보)
     * 3. 배치 발행 → 전체 이벤트 발행 후 ACK 일괄 대기 (효율적인 confirm 수집)
     * 4. 배치 UPDATE → ACK 확인된 ID 목록으로 단일 쿼리 처리
     */
    @Scheduled(fixedDelay = 1000)
    public void publishOutboxEvents() {
        try {
            long start = System.currentTimeMillis();

            // Step 1: 미발행 이벤트 조회 (Spring Data JPA 자체 트랜잭션, 짧게 유지)
            List<OutboxEvent> events = outboxEventRepository.findByPublishedOrderByCreatedAtAsc(false);
            if (events.isEmpty()) {
                return;
            }

            log.info("outbox event 발행 시작: {}건", events.size());

            // Step 2: 모든 이벤트 발행 + CorrelationData 매핑
            // DB 커넥션 없이 RabbitMQ 네트워크 I/O만 수행
            Map<Long, CorrelationData> correlationMap = new LinkedHashMap<>();
            for (OutboxEvent event : events) {
                CorrelationData correlationData = new CorrelationData(String.valueOf(event.getId()));
                try {
                    publishEvent(event, correlationData);
                    correlationMap.put(event.getId(), correlationData);
                } catch (Exception e) {
                    log.error("이벤트 발행 실패: id={}, type={}", event.getId(), event.getEventType(), e);
                }
            }

            // Step 3: 브로커 ACK 대기 (먼저 보낸 메시지일수록 이미 확인됐을 가능성 높음)
            List<Long> confirmedIds = new ArrayList<>();
            for (Map.Entry<Long, CorrelationData> entry : correlationMap.entrySet()) {
                Long eventId = entry.getKey();
                try {
                    CorrelationData.Confirm confirm = entry.getValue().getFuture().get(5, TimeUnit.SECONDS);
                    if (confirm.isAck()) {
                        confirmedIds.add(eventId);
                        log.info("이벤트 발행 ACK 확인: id={}", eventId);
                    } else {
                        log.warn("이벤트 발행 NACK: id={}, reason={} → 다음 사이클에 재시도", eventId, confirm.getReason());
                    }
                } catch (TimeoutException e) {
                    log.error("Publisher Confirm 타임아웃: id={} → 다음 사이클에 재시도", eventId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Publisher Confirm 인터럽트: id={}", eventId);
                    break;
                } catch (Exception e) {
                    log.error("Publisher Confirm 오류: id={}", eventId, e);
                }
            }

            // Step 4: ACK 확인된 이벤트만 단일 배치 UPDATE (짧은 트랜잭션)
            if (!confirmedIds.isEmpty()) {
                outboxEventRepository.markAsPublished(confirmedIds);
                log.info("published 처리 완료: {}건 / 전체 {}건", confirmedIds.size(), events.size());
            }

            log.info("스케줄링 한 사이클 소요 시간: {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("outbox 스케줄링 실패", e);
        }
    }

    private void publishEvent(OutboxEvent outboxEvent, CorrelationData correlationData) throws JsonProcessingException {
        String eventType = outboxEvent.getEventType();
        String payload = outboxEvent.getPayload();

        switch (eventType) {
            case "ORDER_CREATED":
                OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        "order.created.payment",
                        orderCreatedEvent,
                        correlationData
                );
                break;
            case "STOCK_RESTORE":
                StockRestoreEvent stockRestoreEvent = objectMapper.readValue(payload, StockRestoreEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        "stock.restore",
                        stockRestoreEvent,
                        correlationData
                );
                break;
            case "CART_DELETE":
                OrderCreatedEvent cartDeleteEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        "cart.delete",
                        cartDeleteEvent,
                        correlationData
                );
                break;
            default:
                log.warn("알 수 없는 이벤트 타입: {}", eventType);
        }
    }
}
