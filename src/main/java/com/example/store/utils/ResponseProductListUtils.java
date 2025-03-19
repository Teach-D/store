package com.example.store.utils;

import com.example.store.dto.ProductRedisDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class ResponseProductListUtils {

    public static List<ResponseProduct> redisProductListToResponseProductList(List<ProductRedisDto> productRedisDtoList) {
        List<ResponseProduct> responseProductList = new ArrayList<>();
        for (ProductRedisDto product : productRedisDtoList) {
            ResponseProduct responseProduct = ResponseProduct.builder()
                    .categoryId(product.getCategoryId())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .build();

            responseProductList.add(responseProduct);
        }

        return responseProductList;
    }

    public static List<ResponseProduct> productListToResponseProductList(List<Product> products) {
        List<ResponseProduct> responseProductList = new ArrayList<>();
        for (Product product : products) {
            ResponseProduct responseProduct = ResponseProduct.builder()
                    .categoryId(product.getCategory().getId())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .build();

            responseProductList.add(responseProduct);
        }

        return responseProductList;
    }
}
