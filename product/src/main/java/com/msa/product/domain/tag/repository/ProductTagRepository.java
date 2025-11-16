package com.msa.product.domain.tag.repository;

import com.msa.product.domain.tag.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    Optional<ProductTag> findByTagIdAndProductId(Long tagId, Long productId);

}
