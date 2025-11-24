package com.msa.order.domain.order.service;

import com.msa.order.common.client.CartServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsyncCartService {

    private final CartServiceClient cartServiceClient;

    @Async
    public void clearCartItems(List<Long> cartItemIds) {
        for (Long cartItemId : cartItemIds) {
            cartServiceClient.clearCartItem(cartItemId);
        }
    }
}
