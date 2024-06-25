package com.example.store.service;

import com.example.store.dto.AddProductDto;
import com.example.store.dto.EditProductDto;
import com.example.store.entity.Category;
import com.example.store.entity.Product;
import com.example.store.entity.Rating;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public Product addProduct(AddProductDto addProductDto) {
        Category category = categoryService.getCategory(addProductDto.getCategoryId());
        Product product = new Product();
        product.setCategory(category);
        product.setQuantity(addProductDto.getQuantity());
        product.setPrice(addProductDto.getPrice());
        product.setDescription(addProductDto.getDescription());
        product.setImageUrl(addProductDto.getImageUrl());
        product.setTitle(addProductDto.getTitle());
        Rating rating = new Rating();
        rating.setRate(0.0);
        rating.setCount(0);
        product.setRating(rating);

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(Long categoryId, int page, int size) {
        return productRepository.findProductByCategory_id(categoryId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    public Product editProduct(EditProductDto editProductDto, Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setCategory(categoryService.getCategory(editProductDto.getCategoryId()));
        product.setPrice(editProductDto.getPrice());
        product.setDescription(editProductDto.getDescription());
        product.setImageUrl(editProductDto.getImageUrl());
        product.setTitle(editProductDto.getTitle());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        productRepository.delete(product);
    }
}
