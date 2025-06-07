package com.example.store.entity.product;

import com.example.store.entity.Rating;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDetail {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Product product;

    private String description;
    private String imageUrl;

    @Embedded
    private Rating rating;

    @Builder
    public ProductDetail(Product product, String description, String imageUrl, Rating rating) {
        this.product = product;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }
}
