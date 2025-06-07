package com.example.store.repository;

import com.example.store.entity.product.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    Optional<ProductTag> findByTagIdAndProductId(Long tagId, Long productId);

}
