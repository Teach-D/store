package com.example.store.repository;

import com.example.store.entity.Category;
import com.example.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findProductsByCategory_idOrderByPriceAsc(Long categoryId, Pageable pageable);
    Page<Product> findProductsByCategory_id(Long categoryId, Pageable pageable);

    Page<Product> findAllByOrderByPriceDesc(Pageable pageable);
    Page<Product> findAllByOrderByPriceAsc(Pageable pageable);
    Page<Product> findAllByOrderBySaleQuantityDesc(Pageable pageable);
    Page<Product> findAllByOrderBySaleQuantityAsc(Pageable pageable);

    Page<Product> findProductsByCategory_idOrderBySaleQuantityDesc(Long categoryId, Pageable pageable);
    Page<Product> findProductsByCategory_idOrderBySaleQuantityAsc(Long categoryId, Pageable pageable);

    Page<Product> findByCategory_id(Long categoryId, Pageable pageable);
    //List<Product> findByCategory(Category category);
    Page<Product> findAllByCategory(Category category, Pageable pageable);

    Page<Product> findProductsByCategory_idOrderByPriceDesc(Long categoryId, Pageable pageable);

    Product findByTitle(String title);

    List<Product> findByCategory_id(Long categoryId);
}