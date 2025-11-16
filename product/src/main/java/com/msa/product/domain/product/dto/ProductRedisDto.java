package com.msa.product.domain.product.dto;

import com.msa.product.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRedisDto {

    private Long categoryId;
    private int quantity;
    private String title;
    private int price;

    public static ProductRedisDto entityToDto(Product product) {
        return ProductRedisDto.builder()
                .categoryId(product.getCategory().getId())
                .quantity(product.getQuantity())
                .title(product.getTitle())
                .price(product.getPrice())
                .build();
    }
}
