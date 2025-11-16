package com.msa.member.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartItemDto {

    private Long cartItemId;
    private Long productId;
    private int productPrice;
    private int quantity;
}
