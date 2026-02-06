package com.msa.product.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "product_order_stats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_gender_age", columnNames = {"product_id", "gender", "age_group"})
    },
    indexes = {
        @Index(name = "idx_pos_product_id", columnList = "product_id")
    }
)
public class ProductOrderStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(nullable = false)
    @Builder.Default
    private int orderCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalQuantity = 0;

    public void addOrder(int quantity) {
        this.orderCount++;
        this.totalQuantity += quantity;
    }
}
