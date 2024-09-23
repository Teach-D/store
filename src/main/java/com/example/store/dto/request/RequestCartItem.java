package com.example.store.dto.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCartItem {
    private Long cartId;
    private int quantity;
}