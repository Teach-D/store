package com.msa.product.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreatedEvent {
    private Long productId;
    private String title;
    private String description;
    private String categoryName;
    private int price;
}
