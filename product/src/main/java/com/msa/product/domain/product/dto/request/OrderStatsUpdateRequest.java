package com.msa.product.domain.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatsUpdateRequest {
    private Long productId;
    private String gender;
    private String ageGroup;
    private int quantity;
}
