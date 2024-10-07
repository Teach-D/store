package com.example.store.dto.response;

import com.example.store.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseReview {

    private Long id;
    private String title;
    private String content;
    private int rating;
    private String writerName;
    private LocalDateTime createdDate;

    public Page<ResponseReview> toDtoPage(Page<Review> reviewPage) {
        return reviewPage.map(r -> ResponseReview.builder()
                .id(r.getId())
                .rating(r.getRating())
                .content(r.getContent())
                .title(r.getTitle())
                .createdDate(r.getCreateTime())
                .writerName(r.getWriterName())
                .build());
    }
}
