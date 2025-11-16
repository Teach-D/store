package com.msa.member.common.client;

import com.msa.member.global.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@Service
@FeignClient(name = "product-service", url = "${product.service.url}", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}/quantity")
    int getProductQuantity(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/saleQuantity")
    int getProductSaleQuantity(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/price")
    int getProductPrice(@PathVariable("productId") Long productId);


    @PutMapping("/products/{productId}/quantity/{quantity}")
    void updateProductQuantity(@PathVariable("productId") Long productId, @PathVariable("quantity") int quantity);

    @PutMapping("/products/{productId}/saleQuantity/{saleQuantity}")
    void updateProductSaleQuantity(@PathVariable("productId") Long productId, @PathVariable("saleQuantity") int saleQuantity);

}
