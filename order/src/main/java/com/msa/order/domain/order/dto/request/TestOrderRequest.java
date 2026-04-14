package com.msa.order.domain.order.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TestOrderRequest {

    private Long memberId;
    private String date;           // yyyyMMdd 형식, null이면 오늘 날짜 사용
    private List<TestOrderItem> items;

    @Getter
    @NoArgsConstructor
    public static class TestOrderItem {
        private Long sellerId;
        private Long productId;
        private int unitPrice;
        private int quantity;
    }
}
