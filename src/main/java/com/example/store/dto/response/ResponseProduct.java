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

    public static ResponseProduct entityToDto(Product product) {
        return ResponseProduct.builder()
                .quantity(product.getQuantity())
                .title(product.getTitle())
                .price(product.getPrice())
                .build();
    }

    public Page<ResponseProduct> toDtoPage(Page<Product> productPage) {
        return productPage.map(m -> ResponseProduct.builder()
                .quantity(m.getQuantity())
                .title(m.getTitle())
                .price(m.getPrice())
                .build());
    }
}
