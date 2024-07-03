package com.example.store.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {
    private String title;

    private int price;

    private String description;

    private Long categoryId;

    private String imageUrl;

    private int quantity;
}
