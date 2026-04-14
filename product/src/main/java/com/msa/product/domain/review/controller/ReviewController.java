package com.msa.product.domain.review.controller;

import com.msa.product.domain.review.dto.ReviewSummaryResult;
import com.msa.product.domain.review.dto.request.RequestReview;
import com.msa.product.domain.review.dto.response.ResponseReview;
import com.msa.product.domain.review.service.ReviewService;
import com.msa.product.global.dto.NoOffsetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public ResponseEntity<ResponseReview> getReview(@PathVariable Long reviewId) {
        return ResponseEntity.status(OK).body(reviewService.getReviewById(reviewId));
    }

    // 기존 Offset 방식 (하위 호환)
    @GetMapping
    public ResponseEntity<Page<ResponseReview>> getReviewsByProduct(
            @RequestParam(required = false, defaultValue = "0") Long productId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") String sort,
            @RequestParam(required = false, defaultValue = "0") String order
    ) {
        Page<ResponseReview> reviews = reviewService.getByProductId(productId, page);
        return ResponseEntity.status(OK).body(reviews);
    }

    // No-Offset 방식: 첫 요청은 lastId=0, 이후엔 응답의 lastId 값을 전달
    @GetMapping("/no-offset")
    public ResponseEntity<NoOffsetResponse<ResponseReview>> getReviewsNoOffset(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "0") Long lastId
    ) {
        return ResponseEntity.status(OK).body(reviewService.getByProductIdNoOffset(productId, lastId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity addReview(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestReview requestReview, BindingResult bindingResult, @PathVariable Long productId) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        reviewService.createReview(userId, productId, requestReview);
        return ResponseEntity.status(OK).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity editReview(@Valid @RequestBody RequestReview requestReview, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        reviewService.updateReview(id, requestReview);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    // AI 리뷰 요약 — Redis 캐시 반환 (분석 전이면 null)
    @GetMapping("/summary/{productId}")
    public ResponseEntity<ReviewSummaryResult> getReviewSummary(@PathVariable Long productId) {
        ReviewSummaryResult summary = reviewService.getReviewSummary(productId);
        if (summary == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(summary);
    }
}
