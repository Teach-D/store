package com.example.store.mapper;

import com.example.store.entity.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> getProductsByOption(@Param("categoryId") Long categoryId,
                                                 @Param("title") String title,
                                                 @Param("sort") String sort,
                                                 @Param("order") String order
    );
}