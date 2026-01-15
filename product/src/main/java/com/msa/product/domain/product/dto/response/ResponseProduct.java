package com.msa.product.domain.product.dto.response;

import com.msa.product.domain.product.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProduct {

    private Long id;
    private Long categoryId;
    private int quantity;
    private String title;
    private int price;
    private String imageUrl;

    public static ResponseProduct entityToDto(Product product) {
        String imageUrl = null;
        if (product.getProductDetail() != null) {
            imageUrl = product.getProductDetail().getImageUrl();
        }

        return ResponseProduct.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .quantity(product.getQuantity())
                .title(product.getTitle())
                .price(product.getPrice())
                .imageUrl(imageUrl)
                .build();
    }

    public Page<ResponseProduct> toDtoPage(Page<Product> productPage) {
        return productPage.map(m -> {
            String imageUrl = null;
            if (m.getProductDetail() != null) {
                imageUrl = m.getProductDetail().getImageUrl();
            }

            return ResponseProduct.builder()
                    .categoryId(m.getCategory().getId())
                    .quantity(m.getQuantity())
                    .title(m.getTitle())
                    .price(m.getPrice())
                    .imageUrl(imageUrl)
                    .build();
        });
    }
}
