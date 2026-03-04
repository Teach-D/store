package com.msa.product.domain.product.service;

import com.msa.product.domain.product.dto.response.ResponseProduct;
import com.msa.product.domain.product.entity.*;
import com.msa.product.domain.product.repository.ProductOrderStatsRepository;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.product.repository.ProductReviewStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final RankingService rankingService;

    // Write-Behind: DB 대신 Redis에 누적
    public void updateOrderStats(Long productId, Gender gender, AgeGroup ageGroup, int quantity) {
        String key = productId + ":" + gender.name() + ":" + ageGroup.name();

        stringRedisTemplate.opsForValue().increment("stats:order:" + key + ":count");
        stringRedisTemplate.opsForValue().increment("stats:order:" + key + ":quantity", quantity);
        stringRedisTemplate.opsForSet().add("stats:order:dirty", key);

        rankingService.incrementRanking(productId, quantity);

        log.info("[Write-Behind] Redis 누적: productId={}, gender={}, ageGroup={}, quantity={}",
                productId, gender, ageGroup, quantity);
    }

    // 스케줄러 호출용 — Redis 누적값을 DB에 반영
    @Transactional
    public void flushOrderStats(Long productId, Gender gender, AgeGroup ageGroup,
                                int countDelta, int quantityDelta) {
        ProductOrderStats stats = orderStatsRepository
                .findByProductIdAndGenderAndAgeGroup(productId, gender, ageGroup)
                .orElseGet(() -> orderStatsRepository.save(
                        ProductOrderStats.builder()
                                .productId(productId)
                                .gender(gender)
                                .ageGroup(ageGroup)
                                .build()
                ));

        stats.addOrderBulk(countDelta, quantityDelta);
        log.info("[Write-Behind] DB 반영: productId={}, +count={}, +quantity={}",
                productId, countDelta, quantityDelta);
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
