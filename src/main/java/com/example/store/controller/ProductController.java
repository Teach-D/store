package com.example.store.controller;

import com.example.store.dto.AddProductDto;
import com.example.store.dto.EditProductDto;
import com.example.store.dto.ResponseProductDto;
import com.example.store.entity.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product addProduct(@IfLogin LoginUserDto loginUserDto, @RequestBody AddProductDto addProductDto) {
        log.info(loginUserDto.getRoles().toString());
        return productService.addProduct(addProductDto);
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product editProduct(@RequestBody EditProductDto editProductDto, @PathVariable Long id) {
        return productService.editProduct(editProductDto, id);
    }

    @GetMapping
    public Page<Product> getProducts(@RequestParam(required = false, defaultValue = "0") Long categoryId, @RequestParam(required = false, defaultValue = "0") int page) {
        int size = 10;
        if(categoryId == 0)
            return productService.getProducts(page, size);
        else
            return productService.getProducts(categoryId, page, size);
    }

    @GetMapping("/{id}")
    public ResponseProductDto getProducts(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        ResponseProductDto responseProductDto = ResponseProductDto.builder()
                        .product(product)
                        .build();
        return responseProductDto;
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
