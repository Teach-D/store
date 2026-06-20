package com.msa.product.domain.review;

import com.msa.product.domain.review.dto.ReviewSummaryResult;
import com.msa.product.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryConsumer {

    private final ReviewService reviewService;

    @RabbitListener(queues = "review.summary.ready")
    public void handleReviewSummaryReady(ReviewSummaryResult result) {
        log.info("리뷰 분석 결과 수신: productId={}, sentiment={}, avgRating={}",
                result.getProductId(), result.getSentiment().getLabel(), result.getAvgRating());
        reviewService.cacheReviewSummary(result);
    }
}
