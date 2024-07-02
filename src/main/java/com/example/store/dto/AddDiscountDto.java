package com.example.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDiscountDto {

    private String discountName;

    private int discountPrice;

    private String expirationDate;

    private int quantity;

    private int discountCondition;
}
