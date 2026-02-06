package com.msa.product.domain.product.controller;

import com.msa.product.domain.product.dto.request.OrderStatsUpdateRequest;
import com.msa.product.domain.product.dto.request.RequestProduct;
import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.entity.AgeGroup;
import com.msa.product.domain.product.entity.Gender;
import com.msa.product.domain.product.service.ProductCacheService;
import com.msa.product.domain.product.service.ProductService;
import com.msa.product.domain.product.service.ProductStatsService;
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
    private final ProductStatsService productStatsService;

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

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ResponseProduct>> getProductByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.status(OK).body(productService.getProductsByCategoryId(categoryId));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<ResponseProduct>> getProductByTagId(@PathVariable Long tagId) {
        return ResponseEntity.status(OK).body(productService.getProductsByTagId(tagId));
    }

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
    public ResponseEntity addProduct(
            @Valid @RequestPart("product") RequestProduct requestProduct,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        try {
            productService.addProduct(requestProduct, image);
            return ResponseEntity.status(OK).build();
        } catch (Exception e) {
            log.error("상품 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 등록에 실패했습니다: " + e.getMessage());
        }
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

    @GetMapping("/search/order-quantity")
    public ResponseEntity<List<ResponseProduct>> searchByOrderQuantity(@RequestParam String keyword) {
        log.info("상품 검색 (주문순): keyword={}", keyword);
        return ResponseEntity.status(OK).body(productService.searchByKeywordOrderByOrderQuantity(keyword));
    }

    @GetMapping("/search/rating")
    public ResponseEntity<List<ResponseProduct>> searchByRating(@RequestParam String keyword) {
        log.info("상품 검색 (별점순): keyword={}", keyword);
        return ResponseEntity.status(OK).body(productService.searchByKeywordOrderByRating(keyword));
    }

    @GetMapping("/category/{categoryId}/order-quantity")
    public ResponseEntity<List<ResponseProduct>> getProductsByCategoryOrderByOrderQuantity(
            @PathVariable Long categoryId
    ) {
        log.info("카테고리별 상품 조회 (주문순): categoryId={}", categoryId);
        return ResponseEntity.status(OK).body(productService.getProductsByCategoryOrderByOrderQuantity(categoryId));
    }

    @PostMapping("/stats/order")
    public ResponseEntity<Void> updateOrderStats(@RequestBody OrderStatsUpdateRequest request) {
        productStatsService.updateOrderStats(
                request.getProductId(),
                Gender.valueOf(request.getGender()),
                AgeGroup.valueOf(request.getAgeGroup()),
                request.getQuantity()
        );
        return ResponseEntity.status(OK).build();
    }

    @GetMapping("/search/order-quantity/filter")
    public ResponseEntity<List<ResponseProduct>> searchByGenderAndAgeOrderByOrderQuantity(
            @RequestParam String keyword,
            @RequestParam String gender,
            @RequestParam String ageGroup
    ) {
        return ResponseEntity.status(OK).body(
                productStatsService.searchByGenderAndAgeOrderByOrderQuantity(
                        keyword, Gender.valueOf(gender), AgeGroup.valueOf(ageGroup))
        );
    }

    @GetMapping("/search/rating/filter")
    public ResponseEntity<List<ResponseProduct>> searchByGenderAndAgeOrderByRating(
            @RequestParam String keyword,
            @RequestParam String gender,
            @RequestParam String ageGroup
    ) {
        return ResponseEntity.status(OK).body(
                productStatsService.searchByGenderAndAgeOrderByAvgRating(
                        keyword, Gender.valueOf(gender), AgeGroup.valueOf(ageGroup))
        );
    }
}
