package com.example.store.service;

import com.example.store.dto.request.RequestReview;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseReview;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Member;
import com.example.store.entity.product.Product;
import com.example.store.entity.Review;
import com.example.store.repository.ReviewRepository;
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

    public ResponseDto<ResponseReview> getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        ResponseReview responseReview = ResponseReview.builder()
                .writerName(review.getWriterName()).id(reviewId).title(review.getTitle()).content(review.getContent()).rating(review.getRating()).createdDate(review.getCreateTime()).build();

        return ResponseDto.success(responseReview);
    }

    public Page<ResponseReview> getByProductId(Long productId, int page) {
        int size = 10;
        Page<Review> review = reviewRepository.findByProductId(productId, PageRequest.of(page, size));
        Page<ResponseReview> responseReview = new ResponseReview().toDtoPage(review);

        return responseReview;
    }

    public ResponseEntity<SuccessDto> createReview(Member member, Product product, RequestReview requestReview) {
        Review review = Review.builder().createTime(LocalDateTime.now()).product(product).writerName(member.getName()).title(requestReview.getTitle())
                .rating(requestReview.getRating()).content(requestReview.getContent()).build();

        reviewRepository.save(review);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> updateReview(Long beforeReviewId, RequestReview afterReview) {
        Review review = reviewRepository.findById(beforeReviewId).orElse(null);
        review.update(afterReview);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
