package com.msa.order.common.client;

import lombok.Getter;

@Getter
public class CartItemDto {

    private Long cartItemId;
    private Long productId;
    private int productPrice;
    private int quantity;
}
