package com.msa.product.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "product_review_stats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_product_gender_age", columnNames = {"product_id", "gender", "age_group"})
    },
    indexes = {
        @Index(name = "idx_prs_product_id", columnList = "product_id")
    }
)
public class ProductReviewStats {

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
    private int reviewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalScore = 0;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    public void addReview(int rating) {
        this.reviewCount++;
        this.totalScore += rating;
        this.avgScore = BigDecimal.valueOf(this.totalScore)
                .divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
    }
}
