package com.msa.product.domain.product.dto;

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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockRestoreItem implements Serializable {
        private Long productId;
        private int quantity;
    }
}
