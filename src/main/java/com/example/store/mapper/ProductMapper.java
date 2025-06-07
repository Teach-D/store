package com.example.store.mapper;

import com.example.store.entity.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

    Product findById(@Param("id") Long id);
}
