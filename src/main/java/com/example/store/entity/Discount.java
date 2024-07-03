package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discountName;

    private int discountPrice;

    private String expirationDate;

    private int quantity;

    private int discountCondition;

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void updateDiscount(String discountName, int discountPrice, String expirationDate, int quantity) {
        this.discountName = discountName;
        this.discountPrice = discountPrice;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }
}
