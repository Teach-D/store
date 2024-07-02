package com.example.store.dto;

import lombok.Data;

@Data
public class EditProductDto {
    private String title;

    private int price;

    private String description;

    private Long categoryId;

    private String imageUrl;

    private int quantity;
}
