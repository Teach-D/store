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
        Product product = Product.builder()
                        .category(category)
                        .quantity(addProductDto.getQuantity())
                        .price(addProductDto.getPrice())
                        .description(addProductDto.getDescription())
                        .imageUrl(addProductDto.getImageUrl())
                        .title(addProductDto.getTitle())
                        .build();

        Rating rating = Rating.builder()
                        .rate(0.0)
                        .count(0)
                        .build();

        product.updateRating(rating);

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
        product.updateProduct(
                categoryService.getCategory(editProductDto.getCategoryId()),
                editProductDto.getPrice(),
                editProductDto.getDescription(),
                editProductDto.getImageUrl(),
                editProductDto.getTitle()
        );

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        productRepository.delete(product);
    }
}
