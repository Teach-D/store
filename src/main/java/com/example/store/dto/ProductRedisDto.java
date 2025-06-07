package com.example.store.dto;

import com.example.store.entity.product.Product;
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
