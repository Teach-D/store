package com.example.store.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

}
