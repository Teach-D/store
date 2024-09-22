package com.example.store.dto.response;

import com.example.store.entity.Product;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCartItem {

    private Long id;
    private Product product;
    private int quantity;
}
