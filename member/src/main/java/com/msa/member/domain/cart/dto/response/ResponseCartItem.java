package com.msa.member.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCartItem {

    private Long id;
    private Long productId;
    private int quantity;
}
