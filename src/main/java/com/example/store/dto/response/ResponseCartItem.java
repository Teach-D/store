package com.example.store.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCartItem {

    private Long id;
    private Long productId;
    private int quantity;
}
