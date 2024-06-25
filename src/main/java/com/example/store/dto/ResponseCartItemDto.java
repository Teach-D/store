package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.Data;

@Data
public class ResponseCartItemDto {

    private Product product;
    private int quantity;
}
