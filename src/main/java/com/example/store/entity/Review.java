package com.example.store.entity;

import com.example.store.dto.request.RequestReview;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.ResponseReview;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Date;

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

    private String writerName;

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
