package com.msa.product.domain.product.repository;

import com.msa.product.domain.product.entity.AgeGroup;
import com.msa.product.domain.product.entity.Gender;
import com.msa.product.domain.product.entity.ProductReviewStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewStatsRepository extends JpaRepository<ProductReviewStats, Long> {

    Optional<ProductReviewStats> findByProductIdAndGenderAndAgeGroup(Long productId, Gender gender, AgeGroup ageGroup);
}
