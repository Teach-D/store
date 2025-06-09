package com.example.store.mapper;

import com.example.store.entity.product.Product;
import com.example.store.entity.product.ProductAfterIndex;
import com.example.store.entity.product.ProductBeforeIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(@Param("id") Long id);
    List<ProductAfterIndex> getProductsByOptionBeforeIndex(@Param("categoryId") Long categoryId,
                                                @Param("title") String title,
                                                @Param("sort") String sort,
                                                @Param("order") String order
                                      );
    List<ProductAfterIndex> getProductsByOptionAfterIndex(@Param("categoryId") Long categoryId,
                                                           @Param("title") String title,
                                                           @Param("sort") String sort,
                                                           @Param("order") String order
    );
}
