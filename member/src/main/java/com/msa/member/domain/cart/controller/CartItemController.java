package com.msa.member.domain.cart.controller;

import com.msa.member.domain.cart.dto.CartItemDto;
import com.msa.member.domain.cart.dto.request.RequestCartItem;
import com.msa.member.domain.cart.dto.response.ResponseCartItem;
import com.msa.member.domain.cart.service.CartItemService;
import com.msa.member.domain.cart.service.CartService;
import com.msa.member.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cartItems")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;
    private final MemberService memberService;
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<ResponseCartItem>> getCartItems(@RequestHeader("X-User-Id") Long userId) {
        List<ResponseCartItem> cartItems = cartItemService.findCartItems(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cartItems);
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItemDto> getCartItemDtos(@PathVariable Long cartId) {
        return cartItemService.getCartItemDtos(cartId);
    }


    @GetMapping("/{cartItemId}")
    public ResponseEntity<ResponseCartItem> findCartItem(@PathVariable Long cartItemId) {
        ResponseCartItem cartItem = cartItemService.findCartItem(cartItemId);
        return  ResponseEntity.status(HttpStatus.OK).body(cartItem);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addCartItem(@RequestBody RequestCartItem requestCartItem, @PathVariable Long productId) {
        cartItemService.addCartItem(requestCartItem, productId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartItemService.deleteCartItem(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/clear/{cartItemId}")
    public ResponseEntity<Void> clearCartItem(@PathVariable Long cartItemId) {
        cartItemService.clearCartItem(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}