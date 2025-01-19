package com.example.store.repository;

import com.example.store.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    public Page<Review> findByProductId(Long productId, Pageable pageable);
    Optional<Review> findByTitle(String title);
}
