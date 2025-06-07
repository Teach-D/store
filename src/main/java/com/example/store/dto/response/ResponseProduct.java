package com.example.store.dto.response;

import com.example.store.entity.product.Product;
import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProduct {

    private Long categoryId;
    private int quantity;
    private String title;
    private int price;

    public Page<ResponseProduct> toDtoPage(Page<Product> productPage) {
        return productPage.map(m -> ResponseProduct.builder()
                .categoryId(m.getCategory().getId())
                .quantity(m.getQuantity())
                .title(m.getTitle())
                .price(m.getPrice())
                .build());
    }
}
