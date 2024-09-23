package com.example.store.dto.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestProduct {
    private String title;

    private int price;

    private String description;

    private Long categoryId;

    private String imageUrl;

    private int quantity;
}
