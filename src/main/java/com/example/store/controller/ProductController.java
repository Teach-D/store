package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.entity.OrderItem;
import com.example.store.entity.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.OrderItemService;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final OrderItemService orderItemService;

    @GetMapping
    public ResponseDto<Page<Product>> getProducts(@RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page) {
        return productService.getProducts(categoryId, page);
    }

    @GetMapping("/{id}")
    public ResponseDto<ResponseProductDto> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseDto<ResponseProductDto> addProduct(@IfLogin LoginUserDto loginUserDto, @RequestBody AddProductDto addProductDto) {
        return productService.addProduct(addProductDto);
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<SuccessDto> editProduct(@RequestBody EditProductDto editProductDto, @PathVariable Long id) {
        return productService.editProduct(editProductDto, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
