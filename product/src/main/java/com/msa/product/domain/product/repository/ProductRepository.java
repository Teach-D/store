package com.msa.product.domain.product.repository;

import com.msa.product.domain.category.entity.Category;
import com.msa.product.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN order_item oi ON oi.product_id = p.id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
        GROUP BY p.id
        ORDER BY COALESCE(SUM(oi.quantity), 0) DESC
        """, nativeQuery = true)
    List<Product> findByTitleContainingOrderByOrderQuantityDesc(@Param("keyword") String keyword);

    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN review r ON r.product_id = p.id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
        GROUP BY p.id
        ORDER BY COALESCE(AVG(r.rating), 0) DESC
        """, nativeQuery = true)
    List<Product> findByTitleContainingOrderByAvgRatingDesc(@Param("keyword") String keyword);

    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN order_item oi ON oi.product_id = p.id
        WHERE p.category_id = :categoryId
        GROUP BY p.id
        ORDER BY COALESCE(SUM(oi.quantity), 0) DESC
        """, nativeQuery = true)
    List<Product> findByCategoryOrderByOrderQuantityDesc(@Param("categoryId") Long categoryId);
    Page<Product> findProductsByCategory_idOrderByPriceAsc(Long categoryId, Pageable pageable);
    Page<Product> findProductsByCategory_id(Long categoryId, Pageable pageable);

    Page<Product> findAllByOrderByPriceDesc(Pageable pageable);
    Page<Product> findAllByOrderByPriceAsc(Pageable pageable);
    Page<Product> findAllByOrderBySaleQuantityDesc(Pageable pageable);
    Page<Product> findAllByOrderBySaleQuantityAsc(Pageable pageable);

    Page<Product> findProductsByCategory_idOrderBySaleQuantityDesc(Long categoryId, Pageable pageable);
    Page<Product> findProductsByCategory_idOrderBySaleQuantityAsc(Long categoryId, Pageable pageable);

    Page<Product> findByCategory_id(Long categoryId, Pageable pageable);
    Page<Product> findAllByCategory(Category category, Pageable pageable);

    Page<Product> findProductsByCategory_idOrderByPriceDesc(Long categoryId, Pageable pageable);

    Product findByTitle(String title);

    List<Product> findByCategory_id(Long categoryId);

    // =====================================================================
    // 역정규화 이전 쿼리 (문제 사례)
    // order_item → orders → member 다중 JOIN으로 인한 슬로우 쿼리
    // =====================================================================

    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN order_item oi ON oi.product_id = p.id
        LEFT JOIN orders o ON o.order_id = oi.order_id
        LEFT JOIN member m ON m.member_id = o.member_id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
          AND m.gender = :gender
          AND m.birth_date BETWEEN :startDate AND :endDate
        GROUP BY p.id
        ORDER BY COALESCE(SUM(oi.quantity), 0) DESC
        """, nativeQuery = true)
    List<Product> findByKeywordAndGenderAndBirthDateOrderByOrderQuantity(
        @Param("keyword") String keyword,
        @Param("gender") String gender,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN review r ON r.product_id = p.id
        LEFT JOIN member m ON m.member_id = r.member_id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
          AND m.gender = :gender
          AND m.birth_date BETWEEN :startDate AND :endDate
        GROUP BY p.id
        ORDER BY COALESCE(AVG(r.rating), 0) DESC
        """, nativeQuery = true)
    List<Product> findByKeywordAndGenderAndBirthDateOrderByAvgRating(
        @Param("keyword") String keyword,
        @Param("gender") String gender,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    // =====================================================================
    // 역정규화 이후 쿼리 (개선된 버전)
    // product_order_stats, product_review_stats 조회로 다중 JOIN 제거
    // =====================================================================

    @Query(value = """
        SELECT p.* FROM product p
        INNER JOIN product_order_stats pos ON pos.product_id = p.id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
          AND pos.gender = :gender
          AND pos.age_group = :ageGroup
        ORDER BY pos.total_quantity DESC
        """, nativeQuery = true)
    List<Product> findByKeywordAndGenderAndAgeGroupOrderByOrderQuantity(
        @Param("keyword") String keyword,
        @Param("gender") String gender,
        @Param("ageGroup") String ageGroup
    );

    @Query(value = """
        SELECT p.* FROM product p
        INNER JOIN product_review_stats prs ON prs.product_id = p.id
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
          AND prs.gender = :gender
          AND prs.age_group = :ageGroup
        ORDER BY prs.avg_score DESC
        """, nativeQuery = true)
    List<Product> findByKeywordAndGenderAndAgeGroupOrderByAvgRating(
        @Param("keyword") String keyword,
        @Param("gender") String gender,
        @Param("ageGroup") String ageGroup
    );
}