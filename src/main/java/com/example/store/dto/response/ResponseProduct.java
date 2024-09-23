package com.example.store.dto.response;

import com.example.store.entity.Board;
import com.example.store.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

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

    public Page<ResponseProduct> toDtoPage(Page<Product> productPage) {
        return productPage.map(m -> ResponseProduct.builder()
                .product(m)
                .categoryId(m.getCategory().getId())
                .quantity(m.getQuantity())
                .productTitle(m.getTitle())
                .productPrice(m.getPrice())
                .build());
    }
}
