package com.example.payment.domain.payment.dto;

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
    private int totalPrice;
    public static OrderCreatedEvent of(Long orderId, Long userId, List<Long> cartItemIds, int totalPrice) {
        return new OrderCreatedEvent(orderId, userId, cartItemIds, LocalDateTime.now(), totalPrice);
    }
}
