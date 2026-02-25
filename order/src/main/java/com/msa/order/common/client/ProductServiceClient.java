package com.msa.order.common.client;

import com.msa.order.global.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Service
@FeignClient(name = "product-service", url = "${product.service.url}", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}/quantity")
    int getProductQuantity(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/saleQuantity")
    int getProductSaleQuantity(@PathVariable("productId") Long productId);

    @PutMapping("/products/{productId}/quantity/{quantity}")
    void updateProductQuantity(@PathVariable("productId") Long productId, @PathVariable("quantity") int quantity);

    @PutMapping("/products/{productId}/saleQuantity/{saleQuantity}")
    void updateProductSaleQuantity(@PathVariable("productId") Long productId, @PathVariable("saleQuantity") int saleQuantity);

    @PostMapping("/products/stats/order")
    void updateOrderStats(@RequestBody Map<String, Object> request);

    @GetMapping("/products/{productId}/sellerId")
    Long getProductSellerId(@PathVariable("productId") Long productId);
}
