package com.example.store.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCartItemDto {
    private Long cartId;
    private int quantity;
}