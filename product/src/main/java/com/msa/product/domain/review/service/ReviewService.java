package com.msa.product.domain.review.service;

import com.msa.product.domain.product.entity.Product;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.review.dto.request.RequestReview;
import com.msa.product.domain.review.dto.response.ResponseReview;
import com.msa.product.domain.review.entity.Review;
import com.msa.product.domain.review.repository.ReviewRepository;
import com.msa.product.global.exception.CustomException;
import com.msa.product.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

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

    public void createReview(Long userId, Long productId, RequestReview requestReview) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        Review review = Review.builder().createTime(LocalDateTime.now()).product(product).memberId(userId).title(requestReview.getTitle())
                .rating(requestReview.getRating()).content(requestReview.getContent()).build();

        reviewRepository.save(review);
    }

    public void updateReview(Long beforeReviewId, RequestReview afterReview) {
        Review review = reviewRepository.findById(beforeReviewId).orElse(null);
        review.update(afterReview);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
