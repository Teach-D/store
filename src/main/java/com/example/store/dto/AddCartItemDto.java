package com.example.store.dto;

import lombok.Data;

@Data
public class AddCartItemDto {
    private Long cartId;
    private int quantity;
}