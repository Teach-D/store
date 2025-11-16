package com.msa.product.domain.product.controller;

import com.msa.product.domain.product.dto.request.RequestProduct;
import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.service.ProductCacheService;
import com.msa.product.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductCacheService productCacheService;

    @GetMapping
    public ResponseEntity<List<ResponseProduct>> getProductsByOption(
            @RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") String title,
            @RequestParam(required = false, defaultValue = "0") String sort, @RequestParam(required = false, defaultValue = "0") String order
    ) {
        return ResponseEntity.status(OK).body(productService.getProductsByOption(categoryId, title, sort, order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseProduct> getProduct(@PathVariable Long id) {
        return ResponseEntity.status(OK).body(productService.getProduct(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseProduct> getProductByName(@PathVariable String name) {
        return ResponseEntity.status(OK).body(productService.getProductByName(name));
    }

    // categoryId에 속해 있는 product list 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ResponseProduct>> getProductByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.status(OK).body(productService.getProductsByCategoryId(categoryId));
    }

    // tagId에 속해 있는 product list 조회
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<ResponseProduct>> getProductByTagId(@PathVariable Long tagId) {
        return ResponseEntity.status(OK).body(productService.getProductsByTagId(tagId));
    }

    // tagId에 속해 있는 cached product list 조회(하루동안 특정 조회수보다 적은 조회수면 DB에서 product list 조회)
    @GetMapping("/cached/tag/{tagId}")
    public ResponseEntity<List<ResponseProduct>> getCachedProductByTagId(@PathVariable Long tagId) {
        return ResponseEntity.status(OK).body(productCacheService.getPopularProductsByTagId(tagId));
    }

    @GetMapping("/{productId}/quantity")
    public int getProductQuantity(@PathVariable("productId") Long productId) {
        return productService.getProductQuantity(productId);
    }

    @GetMapping("/{productId}/saleQuantity")
    public int getProductSaleQuantity(@PathVariable("productId") Long productId) {
        return productService.getProductSaleQuantity(productId);
    }

    @GetMapping("/{productId}/price")
    public int getProductPrice(@PathVariable("productId") Long productId) {
        return productService.getProductPrice(productId);

    }

    @PutMapping("/{productId}/quantity/{quantity}")
    public void updateProductQuantity(@PathVariable("productId") Long productId, @PathVariable("quantity") int quantity) {
        productService.updateProductQuantity(productId, quantity);
    }

    @PutMapping("/{productId}/saleQuantity/{saleQuantity}")
    public void updateProductSaleQuantity(@PathVariable("productId") Long productId, @PathVariable("saleQuantity") int saleQuantity) {
        productService.updateProductSaleQuantity(productId, saleQuantity);
    }


    @PostMapping
    public ResponseEntity addProduct(@Valid @RequestBody RequestProduct requestProduct, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        productService.addProduct(requestProduct);
        return ResponseEntity.status(OK).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity editProduct(@Valid @RequestBody RequestProduct requestProduct, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        productService.editProduct(requestProduct, id);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
