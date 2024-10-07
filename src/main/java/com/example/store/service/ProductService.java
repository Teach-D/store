package com.example.store.service;

import com.example.store.dto.request.RequestProduct;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Category;
import com.example.store.entity.OrderItem;
import com.example.store.entity.Product;
import com.example.store.entity.Rating;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final CategoryRepository categoryRepository;

    @Transactional
    public ResponseEntity<SuccessDto> addProduct(RequestProduct requestProduct) {
        Category category = categoryService.getCategory(requestProduct.getCategoryId());
        Product product = Product.builder()
                        .category(category)
                        .quantity(requestProduct.getQuantity())
                        .price(requestProduct.getPrice())
                        .description(requestProduct.getDescription())
                        .imageUrl(requestProduct.getImageUrl())
                        .title(requestProduct.getTitle())
                        .build();

        Rating rating = Rating.builder()
                        .rate(0.0)
                        .count(0)
                        .build();

        product.updateRating(rating);

        productRepository.save(product);

        ResponseProduct responseProduct = ResponseProduct.builder().product(product).build();

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(Long categoryId, int page, int size) {
        return productRepository.findProductsByCategory_id(categoryId, PageRequest.of(page, size));
    }


    @Transactional(readOnly = true)
    public Page<Product> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public ResponseDto<ResponseProduct> getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(NotFoundProductException::new);
        ResponseProduct responseProduct = ResponseProduct.builder()
                .product(product)
                .categoryId(product.getCategory().getId())
                .build();

        return ResponseDto.success(responseProduct);
    }

    public ResponseEntity<SuccessDto> editProduct(RequestProduct requestProduct, Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.updateProduct(
                categoryService.getCategory(requestProduct.getCategoryId()),
                requestProduct.getPrice(),
                requestProduct.getDescription(),
                requestProduct.getImageUrl(),
                requestProduct.getTitle(),
                requestProduct.getQuantity()
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

    public Page<Product> getProducts(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;
        log.info("aa");
        if(categoryId == 0) {
            product = productRepository.findAll(PageRequest.of(page, size));
        } else {
            product = productRepository.findProductsByCategory_id(categoryId, PageRequest.of(page, size));
        }

        return product;
    }

    public Page<Product> getProductsBySaleAsc(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;

        if(categoryId == 0) {
            product = productRepository.findAllByOrderBySaleQuantityAsc(PageRequest.of(page, size));
        } else {
            product = productRepository.findProductsByCategory_idOrderBySaleQuantityAsc(categoryId, PageRequest.of(page, size));
        }

        return product;
    }

    public Page<Product> getProductsBySaleDesc(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;

        if(categoryId == 0) {
            product = productRepository.findAllByOrderBySaleQuantityDesc(PageRequest.of(page, size));
        } else {
            product = productRepository.findProductsByCategory_idOrderBySaleQuantityDesc(categoryId, PageRequest.of(page, size));
        }

        return product;
    }

    public Page<Product> getProductsByPriceAsc(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;

        if (categoryId == 0) {
            product = productRepository.findAllByOrderByPriceAsc(PageRequest.of(page, size));
        } else {
            log.info("price asc");
            log.info(String.valueOf(page));
            product = productRepository.findProductsByCategory_idOrderByPriceAsc(categoryId, PageRequest.of(page, size));

        }

        return product;
    }

    public Page<Product> getProductsByPriceDesc(Long categoryId, int page) {
        int size = 10;
        Page<Product> product = null;

        if (categoryId == 0) {
            product = productRepository.findAllByOrderByPriceDesc(PageRequest.of(page, size));
        } else {
            product = productRepository.findProductsByCategory_idOrderByPriceDesc(categoryId, PageRequest.of(page, size));
        }

        return product;
    }

    public ResponseDto<ResponseProduct> getProductByName(String name) {
        Product product = productRepository.findByTitle(name);
        ResponseProduct responseProduct = ResponseProduct.builder()
                .product(product)
                .build();

        return ResponseDto.success(responseProduct);
    }
}
