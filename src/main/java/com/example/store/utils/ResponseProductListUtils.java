package com.example.store.utils;

import com.example.store.dto.response.ResponseProduct;
import com.example.store.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class ResponseProductListUtils {

    public static List<ResponseProduct> productListToResponseProductList(List<Product> productList) {
        List<ResponseProduct> responseProductList = new ArrayList<>();
        for (Product product : productList) {
            ResponseProduct responseProduct = ResponseProduct.builder()
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .build();

            responseProductList.add(responseProduct);
        }

        return responseProductList;
    }
}
