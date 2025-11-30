package com.example.payment.domain.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedOrderByCreatedAtAsc(boolean published);
}
