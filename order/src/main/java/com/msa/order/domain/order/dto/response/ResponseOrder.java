package com.msa.order.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseOrder {

    private List<Long> productIds = new ArrayList<>();
    private String date;
    private Long id;
    private int totalPrice;

}
