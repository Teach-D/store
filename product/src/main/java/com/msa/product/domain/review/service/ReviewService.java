package com.msa.product.domain.review.service;

import com.msa.product.domain.product.entity.AgeGroup;
import com.msa.product.domain.product.entity.Gender;
import com.msa.product.domain.product.entity.Product;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.product.service.ProductStatsService;
import com.msa.product.domain.review.dto.ReviewCreatedEvent;
import com.msa.product.domain.review.dto.ReviewSummaryResult;
import com.msa.product.domain.review.dto.request.RequestReview;
import com.msa.product.domain.review.dto.response.ResponseReview;
import com.msa.product.domain.review.entity.Review;
import com.msa.product.domain.review.repository.ReviewRepository;
import com.msa.product.global.client.MemberServiceClient;
import com.msa.product.global.config.RabbitMQConfig;
import com.msa.product.global.dto.NoOffsetResponse;
import com.msa.product.global.exception.CustomException;
import com.msa.product.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductStatsService productStatsService;
    private final MemberServiceClient memberServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REVIEW_SUMMARY_CACHE_KEY = "review:summary:";

    public ResponseReview getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        return ResponseReview.builder()
                .memberId(review.getMemberId()).id(reviewId).title(review.getTitle()).content(review.getContent()).rating(review.getRating()).createdDate(review.getCreateTime()).build();

    }

    public Page<ResponseReview> getByProductId(Long productId, int page) {
        int size = 10;
        Page<Review> review = reviewRepository.findByProductId(productId, PageRequest.of(page, size));
        Page<ResponseReview> responseReview = new ResponseReview().toDtoPage(review);

        return responseReview;
    }

    // No-Offset 페이징: lastId = 0이면 첫 페이지, 이후엔 이전 응답의 lastId 전달
    @Transactional(readOnly = true)
    public NoOffsetResponse<ResponseReview> getByProductIdNoOffset(Long productId, Long lastId) {
        int size = 10;
        List<Review> reviews = reviewRepository.findByProductIdNoOffset(productId, lastId, size + 1);

        boolean hasNext = reviews.size() > size;
        if (hasNext) {
            reviews = reviews.subList(0, size);
        }

        List<ResponseReview> content = reviews.stream()
                .map(r -> ResponseReview.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .content(r.getContent())
                        .rating(r.getRating())
                        .memberId(r.getMemberId())
                        .createdDate(r.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        Long nextLastId = content.isEmpty() ? null : content.get(content.size() - 1).getId();
        return new NoOffsetResponse<>(content, hasNext, nextLastId);
    }

    public void createReview(Long userId, Long productId, RequestReview requestReview) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        Review review = Review.builder().createTime(LocalDateTime.now()).product(product).memberId(userId).title(requestReview.getTitle())
                .rating(requestReview.getRating()).content(requestReview.getContent()).build();

        reviewRepository.save(review);

        // AI 리뷰 분석 트리거 — 저장 완료 후 비동기 발행
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.REVIEW_EXCHANGE,
                    RabbitMQConfig.REVIEW_CREATED_QUEUE,
                    new ReviewCreatedEvent(productId, review.getId())
            );
            log.info("review.created 이벤트 발행: productId={}, reviewId={}", productId, review.getId());
        } catch (Exception e) {
            log.warn("review.created 이벤트 발행 실패 (분석은 다음 리뷰 작성 시 재시도): productId={}", productId, e);
        }

        try {
            String genderStr = memberServiceClient.getMemberGender(userId);
            String birthDateStr = memberServiceClient.getMemberBirthDate(userId);
            LocalDateTime birthDate = LocalDateTime.parse(birthDateStr);
            Gender gender = Gender.valueOf(genderStr);
            AgeGroup ageGroup = AgeGroup.fromBirthDate(birthDate);

            productStatsService.updateReviewStats(productId, gender, ageGroup, requestReview.getRating());
        } catch (Exception e) {
            log.warn("리뷰 통계 업데이트 실패: userId={}, productId={}", userId, productId, e);
        }
    }

    public void updateReview(Long beforeReviewId, RequestReview afterReview) {
        Review review = reviewRepository.findById(beforeReviewId).orElse(null);
        review.update(afterReview);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResult getReviewSummary(Long productId) {
        String key = REVIEW_SUMMARY_CACHE_KEY + productId;
        return (ReviewSummaryResult) redisTemplate.opsForValue().get(key);
    }

    public void cacheReviewSummary(ReviewSummaryResult result) {
        String key = REVIEW_SUMMARY_CACHE_KEY + result.getProductId();
        redisTemplate.opsForValue().set(key, result, 24, TimeUnit.HOURS);
        log.info("리뷰 요약 캐시 저장: productId={}, sentiment={}", result.getProductId(), result.getSentiment().getLabel());
    }
}
