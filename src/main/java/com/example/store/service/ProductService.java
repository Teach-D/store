package com.example.store.service;

import com.example.store.dto.request.RequestProduct;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.*;
import com.example.store.entity.product.Product;
import com.example.store.entity.product.ProductDetail;
import com.example.store.entity.product.ProductTag;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final OrderItemRepository orderItemRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ResponseEntity<SuccessDto> addProduct(RequestProduct requestProduct) {
        Category category = categoryService.getCategory(requestProduct.getCategoryId());
        Product product = Product.builder()
                        .category(category)
                        .quantity(requestProduct.getQuantity())
                        .price(requestProduct.getPrice())
                        .title(requestProduct.getTitle())
                        .build();

        Rating rating = Rating.builder()
                        .rate(0.0)
                        .count(0)
                        .build();

        ProductDetail productDetail = ProductDetail.builder()
                .product(product)
                .description(requestProduct.getDescription())
                .imageUrl(requestProduct.getImageUrl())
                .rating(rating)
                .build();


        productRepository.save(product);

        ResponseProduct responseProduct = ResponseProduct.builder().title(product.getTitle()).price(product.getPrice()).build();

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }


    // categoryId로 category에 속해 있는 product list 조회
    @Transactional(readOnly = true)
    public ResponseDto<List<ResponseProduct>> getProductsByCategoryId(Long categoryId) {
        List<ResponseProduct> responseProducts = new ArrayList<>();

        List<Product> productsCategoryId = productRepository.findByCategory_id(categoryId);
        for (Product product : productsCategoryId) {
            ResponseProduct.builder()
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .categoryId(categoryId)
                    .build();
        }

        return ResponseDto.success(responseProducts);
    }

    // categoryId로 category에 속해 있는 product list 조회
    @Transactional(readOnly = true)
    public ResponseDto<List<ResponseProduct>> getProductsByTagId(Long tagId) {
        List<ResponseProduct> responseProducts = new ArrayList<>();

        Tag tag = tagRepository.findById(tagId).get();
        List<ProductTag> productTags = tag.getProductTags();
        for (ProductTag productTag : productTags) {
            Product product = productTag.getProduct();

            ResponseProduct responseProduct = ResponseProduct.builder()
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .categoryId(product.getCategory().getId())
                    .build();

            responseProducts.add(responseProduct);
        }


        return ResponseDto.success(responseProducts);
    }


    @Transactional(readOnly = true)
    public ResponseDto<ResponseProduct> getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(NotFoundProductException::new);
        ResponseProduct responseProduct = ResponseProduct.builder()
                .title(product.getTitle())
                .price(product.getPrice())
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
                .title(product.getTitle())
                .price(product.getPrice())
                .build();

        return ResponseDto.success(responseProduct);
    }

    public List<ResponseProduct> getProductsByOption(Long categoryId, String title, String sort, String order) {
        // categoryId가 0이면 적용x
        Long filteredCategoryId = categoryId != 0 ? categoryId : null;

        // title이 0이면 적용x
        String filteredTitle = "0".equals(title) ? title : null;

        // sort, order가 "0"이면 정렬 제외 처리
        boolean noSort = "0".equals(sort) || "0".equals(order);
        String filteredSort = noSort ? null : sort.toLowerCase();
        String filteredOrder = noSort ? null : order.toLowerCase();

        List<ResponseProduct> responseProductList = new ArrayList<>();

        List<Product> productsByOption = productMapper.getProductsByOption(filteredCategoryId, filteredTitle, filteredSort, filteredOrder);
        for (Product product : productsByOption) {
            ResponseProduct responseProduct = ResponseProduct.entityToDto(product);
            responseProductList.add(responseProduct);
        }

        return responseProductList;
    }
}
