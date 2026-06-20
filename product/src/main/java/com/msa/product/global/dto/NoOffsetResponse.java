package com.msa.product.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NoOffsetResponse<T> {
    private List<T> content;
    private boolean hasNext;
    private Long lastId;   // 다음 요청 시 이 값을 lastId로 전달
}
