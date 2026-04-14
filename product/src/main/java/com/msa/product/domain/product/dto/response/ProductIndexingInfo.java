package com.msa.product.domain.product.dto.response;

import com.msa.product.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductIndexingInfo {

    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private int price;

    public static ProductIndexingInfo from(Product product) {
        String description = "";
        if (product.getProductDetail() != null) {
            description = product.getProductDetail().getDescription();
        }

        String categoryName = "";
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
        }

        return new ProductIndexingInfo(
                product.getId(),
                product.getTitle(),
                description,
                categoryName,
                product.getPrice()
        );
    }
}
