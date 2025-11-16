package com.msa.product.domain.review.repository;

import com.msa.product.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    Optional<Review> findByTitle(String title);
}
