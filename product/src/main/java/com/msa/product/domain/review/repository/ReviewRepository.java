package com.msa.product.domain.review.repository;

import com.msa.product.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 기존 Offset 방식 (하위 호환 유지)
    Page<Review> findByProductId(Long productId, Pageable pageable);

    // No-Offset 방식: idx_review_product_id (product_id, id) 커버링 인덱스 활용
    // lastId = 0이면 첫 페이지, 이후엔 이전 응답의 lastId 전달
    @Query(value = """
            SELECT * FROM review
            WHERE product_id = :productId
              AND (:lastId = 0 OR id < :lastId)
            ORDER BY id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Review> findByProductIdNoOffset(
            @Param("productId") Long productId,
            @Param("lastId") Long lastId,
            @Param("size") int size
    );

    Optional<Review> findByTitle(String title);
}
