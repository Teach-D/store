package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int price;

    private String description;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String imageUrl;

    @Embedded
    private Rating rating;

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void updateProduct(Category category, int price, String description, String imageUrl, String title, int quantity) {
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.title = title;
        this.quantity = quantity;
    }

    public void updateRating(Rating rating) {
        this.rating = rating;
    }
}
