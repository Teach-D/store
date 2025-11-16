package com.msa.order.common.client;

import com.msa.order.global.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Service
@FeignClient(name = "member-service", url = "${member.service.url}", configuration = FeignConfig.class)
public interface CartServiceClient {

    @GetMapping("/carts/{memberId}")
    Long getCartId(@PathVariable Long memberId);

    @GetMapping("/cartItems/cart/{cartId}")
    List<CartItemDto> getCartItems(@PathVariable Long cartId);

    @GetMapping("/coupons/amount/{couponId}")
    int getDiscountAmount(@PathVariable Long couponId);

    @GetMapping("/deliveries/user/{userId}")
    Long getDelivery(@PathVariable Long userId);

    @DeleteMapping("/cartItems/clear/{cartItemId}")
    ResponseEntity<Void> clearCartItem(@PathVariable Long cartItemId);

    @DeleteMapping("/coupons/use/{couponId}")
    ResponseEntity<Void> useCoupon(@PathVariable Long couponId, @RequestHeader("X-User-Id") Long userId);
}
