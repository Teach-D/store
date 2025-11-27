package com.msa.order.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    private Long orderId;
    private Long userId;
    private List<Long> cartItemIds;
    private LocalDateTime createdAt;

    public static OrderCreatedEvent of(Long orderId, Long userId, List<Long> cartItemIds) {
        return new OrderCreatedEvent(orderId, userId, cartItemIds, LocalDateTime.now());
    }
}
