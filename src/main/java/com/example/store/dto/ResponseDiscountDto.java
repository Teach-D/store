package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.Data;

@Data
public class ResponseDiscountDto {
    private Long id;

    private String discountName;

    private int discountPrice;

    private String expirationDate;

    private int quantity;

    private int discountCondition;

}
