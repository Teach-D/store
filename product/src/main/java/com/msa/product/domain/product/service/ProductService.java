package com.msa.product.domain.product.service;

import com.msa.product.domain.category.entity.Category;
import com.msa.product.domain.category.repository.CategoryRepository;
import com.msa.product.domain.category.service.CategoryService;
import com.msa.product.domain.product.dto.request.RequestProduct;
import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.entity.Product;
import com.msa.product.domain.product.entity.ProductDetail;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.review.entity.Rating;
import com.msa.product.domain.tag.entity.ProductTag;
import com.msa.product.domain.tag.entity.Tag;
import com.msa.product.domain.tag.repository.TagRepository;
import com.msa.product.global.exception.CustomException;
import com.msa.product.global.exception.ErrorCode;
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
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ImageUploadService imageUploadService;
    private final com.msa.product.domain.product.repository.ProductDetailRepository productDetailRepository;

    @Transactional
    public void addProduct(RequestProduct requestProduct, org.springframework.web.multipart.MultipartFile image) throws java.io.IOException {
        Category category = categoryService.getCategory(requestProduct.getCategoryId());

        String imageUrl = null;

        // 1. 이미지 파일이 업로드된 경우
        if (image != null && !image.isEmpty()) {
            imageUrl = imageUploadService.saveImage(image);
            log.info("파일 업로드 완료. imageUrl: {}", imageUrl);
        }
        // 2. RequestProduct에 imageUrl이 있는 경우 (외부 URL)
        else if (requestProduct.getImageUrl() != null && !requestProduct.getImageUrl().isEmpty()) {
            imageUrl = requestProduct.getImageUrl();
            log.info("외부 URL 사용. imageUrl: {}", imageUrl);
        }

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
                .imageUrl(imageUrl)
                .rating(rating)
                .build();

        productRepository.save(product);
        productDetailRepository.save(productDetail);

        log.info("상품 등록 완료. productId: {}, imageUrl: {}", product.getId(), imageUrl);
    }


    // categoryId로 category에 속해 있는 product list 조회
    @Transactional(readOnly = true)
    public List<ResponseProduct> getProductsByCategoryId(Long categoryId) {
        log.info("getProductsByCategoryId {}", categoryId);
        List<ResponseProduct> responseProducts = new ArrayList<>();
        List<Product> productsCategoryId;

        if (categoryId == 0) {
            productsCategoryId = productRepository.findAll();
        } else {
            productsCategoryId = productRepository.findByCategory_id(categoryId);
        }

        for (Product product : productsCategoryId) {
            responseProducts.add(ResponseProduct.entityToDto(product));
        }

        return responseProducts;
    }

    // categoryId로 category에 속해 있는 product list 조회
    @Transactional(readOnly = true)
    public List<ResponseProduct> getProductsByTagId(Long tagId) {
        List<ResponseProduct> responseProducts = new ArrayList<>();

        Tag tag = tagRepository.findById(tagId).get();
        List<ProductTag> productTags = tag.getProductTags();
        for (ProductTag productTag : productTags) {
            Product product = productTag.getProduct();
            responseProducts.add(ResponseProduct.entityToDto(product));
        }

        return responseProducts;
    }


    @Transactional(readOnly = true)
    public ResponseProduct getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return ResponseProduct.entityToDto(product);
    }

    public void editProduct(RequestProduct requestProduct, Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.updateProduct(
                categoryService.getCategory(requestProduct.getCategoryId()),
                requestProduct.getPrice(),
                requestProduct.getDescription(),
                requestProduct.getImageUrl(),
                requestProduct.getTitle(),
                requestProduct.getQuantity()
        );
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        productRepository.delete(product);
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

    public ResponseProduct getProductByName(String name) {
        Product product = productRepository.findByTitle(name);
        return ResponseProduct.entityToDto(product);
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

        List<Product> productsByOption = productRepository.findAll();
        for (Product product : productsByOption) {
            ResponseProduct responseProduct = ResponseProduct.entityToDto(product);
            responseProductList.add(responseProduct);
        }

        return responseProductList;
    }

    public int getProductQuantity(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return product.getQuantity();
    }

    public int getProductSaleQuantity(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return product.getSaleQuantity();
    }

    public void updateProductQuantity(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.updateQuantity(quantity);
    }

    public void updateProductSaleQuantity(Long productId, int saleQuantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.updateSaleQuantity(saleQuantity);
    }

    public int getProductPrice(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return product.getPrice();
    }

    public void restoreTock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.updateQuantity(product.getQuantity() + quantity);
        product.updateSaleQuantity(product.getSaleQuantity() - quantity);

        log.info("재고 복구 완료(DB)");
    }
}
