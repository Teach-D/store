package com.msa.order.domain.order.dto;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRestoreEvent implements Serializable {

    private Long orderId;
    private List<StockRestoreItem> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockRestoreItem implements Serializable {
        private Long productId;
        private int quantity;
    }

    public static StockRestoreEvent of(Order order) {
        List<StockRestoreItem> items = order.getOrderItems().stream()
                .map(orderItem -> StockRestoreItem.builder()
                        .productId(orderItem.getProductId())
                        .quantity(orderItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return StockRestoreEvent.builder()
                .orderId(order.getOrderId())
                .items(items)
                .build();
    }
}
