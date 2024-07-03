package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCartItemDto {

    private Product product;
    private int quantity;
}
