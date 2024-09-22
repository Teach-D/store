package com.example.store.dto.response;

import com.example.store.entity.Product;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProduct {

    private Product product;
    private Long categoryId;
    private int quantity;
    private String productTitle;
    private int productPrice;
}
