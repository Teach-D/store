package com.msa.product.domain.product.service;
import com.msa.product.domain.product.dto.ProductRedisDto;
import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.entity.Product;

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
