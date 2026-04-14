package com.msa.product.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResult {

    private Long productId;
    private String summary;
    private SentimentResult sentiment;
    private int reviewCount;
    private double avgRating;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentResult {
        private String label;               // POSITIVE | NEGATIVE | MIXED
        private List<String> positiveKeywords;
        private List<String> negativeKeywords;
    }
}
