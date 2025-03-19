package com.example.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int price;

    private String description;

    private int quantity;

    private int saleQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private final List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    @JsonIgnore
    private final List<ProductTag> productTags = new ArrayList<>();

    private String imageUrl;

    @Embedded
    private Rating rating;

    public Product(Product p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.price = p.getPrice();
        this.description = p.getDescription();
        this.quantity = p.getQuantity();
        this.category = p.getCategory();
        this.imageUrl = p.getImageUrl();
        this.rating = p.getRating();
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
        this.description = description;
        this.imageUrl = imageUrl;
        this.title = title;
        this.quantity = quantity;
    }

    public void updateRating(Rating rating) {
        this.rating = rating;
    }
}
