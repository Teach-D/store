package com.msa.product.domain.review.entity;

import com.msa.product.domain.product.entity.Product;
import com.msa.product.domain.review.dto.request.RequestReview;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 별점
    private int rating;

    private String title;

    private String content;

    private Long memberId;

    @CreationTimestamp
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public void update(RequestReview afterReview) {
        this.content = afterReview.getContent();
        this.title = afterReview.getTitle();
        this.rating = afterReview.getRating();
    }
}
