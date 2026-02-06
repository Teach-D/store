package com.msa.product.domain.product.repository;

import com.msa.product.domain.product.entity.AgeGroup;
import com.msa.product.domain.product.entity.Gender;
import com.msa.product.domain.product.entity.ProductOrderStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductOrderStatsRepository extends JpaRepository<ProductOrderStats, Long> {

    Optional<ProductOrderStats> findByProductIdAndGenderAndAgeGroup(Long productId, Gender gender, AgeGroup ageGroup);
}
