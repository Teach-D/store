package com.msa.product.domain.product.service;

import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.entity.*;
import com.msa.product.domain.product.repository.ProductOrderStatsRepository;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.product.repository.ProductReviewStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductStatsService {

    private final ProductOrderStatsRepository orderStatsRepository;
    private final ProductReviewStatsRepository reviewStatsRepository;
    private final ProductRepository productRepository;

    public void updateOrderStats(Long productId, Gender gender, AgeGroup ageGroup, int quantity) {
        ProductOrderStats stats = orderStatsRepository
                .findByProductIdAndGenderAndAgeGroup(productId, gender, ageGroup)
                .orElseGet(() -> orderStatsRepository.save(
                        ProductOrderStats.builder()
                                .productId(productId)
                                .gender(gender)
                                .ageGroup(ageGroup)
                                .build()
                ));

        stats.addOrder(quantity);
        log.info("주문 통계 업데이트: productId={}, gender={}, ageGroup={}, quantity={}",
                productId, gender, ageGroup, quantity);
    }

    public void updateReviewStats(Long productId, Gender gender, AgeGroup ageGroup, int rating) {
        ProductReviewStats stats = reviewStatsRepository
                .findByProductIdAndGenderAndAgeGroup(productId, gender, ageGroup)
                .orElseGet(() -> reviewStatsRepository.save(
                        ProductReviewStats.builder()
                                .productId(productId)
                                .gender(gender)
                                .ageGroup(ageGroup)
                                .build()
                ));

        stats.addReview(rating);
        log.info("리뷰 통계 업데이트: productId={}, gender={}, ageGroup={}, rating={}",
                productId, gender, ageGroup, rating);
    }

    @Transactional(readOnly = true)
    public List<ResponseProduct> searchByGenderAndAgeOrderByOrderQuantity(
            String keyword, Gender gender, AgeGroup ageGroup) {
        List<Product> products = productRepository
                .findByKeywordAndGenderAndAgeGroupOrderByOrderQuantity(keyword, gender.name(), ageGroup.name());

        List<ResponseProduct> result = new ArrayList<>();
        for (Product product : products) {
            result.add(ResponseProduct.entityToDto(product));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ResponseProduct> searchByGenderAndAgeOrderByAvgRating(
            String keyword, Gender gender, AgeGroup ageGroup) {
        List<Product> products = productRepository
                .findByKeywordAndGenderAndAgeGroupOrderByAvgRating(keyword, gender.name(), ageGroup.name());

        List<ResponseProduct> result = new ArrayList<>();
        for (Product product : products) {
            result.add(ResponseProduct.entityToDto(product));
        }
        return result;
    }
}
