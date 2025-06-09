package com.example.store.entity.product;

import com.example.store.entity.Category;
import com.example.store.entity.Review;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductBeforeIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int price;

    private int quantity;

    private int saleQuantity;
/*
    private String description;
    private String imageUrl;

    @Embedded
    private Rating rating;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;

    public ProductBeforeIndex(ProductBeforeIndex p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.price = p.getPrice();
        this.quantity = p.getQuantity();
        this.category = p.getCategory();
        this.saleQuantity = p.getSaleQuantity();
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void updateSaleQuantity(int saleQuantity) {
        this.saleQuantity = saleQuantity;
    }

    public void updateProduct(Category category, int price, String description, String imageUrl, String title, int quantity) {
        this.category = category;
        this.price = price;
        this.title = title;
        this.quantity = quantity;
    }

/*    public void updateRating(Rating rating) {
        this.rating = rating;
    }*/
}
