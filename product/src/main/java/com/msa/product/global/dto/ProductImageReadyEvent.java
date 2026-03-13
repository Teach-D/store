package com.msa.product.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageReadyEvent {
    private Long productId;
    private String imageUrl;
    private String promoImageUrl;
}
