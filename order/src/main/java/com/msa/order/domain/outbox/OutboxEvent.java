package com.msa.order.domain.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "idx_published_created",
        indexes = {
                @Index(name = "idx_published_created",
                columnList = "published, createdAt")
        }
)
@NoArgsConstructor
@Getter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;

    @Lob
    private String payload;

    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean published = false;

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void changeAsPublished() {
        this.published = true;
    }
}
