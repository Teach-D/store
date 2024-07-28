package com.example.store.service;

import com.example.store.dto.*;
import com.example.store.entity.Category;
import com.example.store.entity.OrderItem;
import com.example.store.entity.Product;
import com.example.store.entity.Rating;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ResponseDto<ResponseProductDto> addProduct(AddProductDto addProductDto) {
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

        productRepository.save(product);

        ResponseProductDto responseProductDto = ResponseProductDto.builder().product(product).build();

        return ResponseDto.success(responseProductDto);
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
    public ResponseDto<ResponseProductDto> getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(NotFoundProductException::new);
        ResponseProductDto responseProductDto = ResponseProductDto.builder()
                .product(product)
                .build();

        return ResponseDto.success(responseProductDto);
    }

    public ResponseEntity<SuccessDto> editProduct(EditProductDto editProductDto, Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.updateProduct(
                categoryService.getCategory(editProductDto.getCategoryId()),
                editProductDto.getPrice(),
                editProductDto.getDescription(),
                editProductDto.getImageUrl(),
                editProductDto.getTitle(),
                editProductDto.getQuantity()
        );

        log.info(String.valueOf(product.getQuantity()));

        productRepository.save(product);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteProduct(Long id) {

        for (OrderItem orderItem : orderItemRepository.findByProductId(id)) {
            orderItem.updateProduct();
            orderItem.deleteProduct();
        }

        Product product = productRepository.findById(id).orElseThrow();
        productRepository.delete(product);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseDto<Page<Product>> getProducts(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;

        if(categoryId == 0) {
            product = productRepository.findAll(PageRequest.of(page, size));
        } else {
            product = productRepository.findProductByCategory_id(categoryId, PageRequest.of(page, size));
        }

        return ResponseDto.success(product);
    }
}
