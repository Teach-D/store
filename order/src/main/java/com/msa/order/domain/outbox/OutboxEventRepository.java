package com.msa.order.domain.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedOrderByCreatedAtAsc(boolean published);

    // 브로커 ACK 확인된 이벤트만 배치로 published=true 처리
    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent e SET e.published = true WHERE e.id IN :ids")
    void markAsPublished(@Param("ids") List<Long> ids);
}
