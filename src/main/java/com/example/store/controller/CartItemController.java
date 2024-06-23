package com.example.store.controller;

import com.example.store.dto.AddCartItemDto;
import com.example.store.entity.CartItem;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cartItems")
@RestController
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public CartItem addCartItem(@IfLogin LoginUserDto loginUserDto, @RequestBody AddCartItemDto addCartItemDto) {
        if(cartItemService.isCartItemExist(loginUserDto.getMemberId(), addCartItemDto.getCartId(), addCartItemDto.getProductId())) {
            CartItem cartItem = cartItemService.getCartItem(loginUserDto.getMemberId(), addCartItemDto.getCartId(), addCartItemDto.getProductId());
            cartItem.setQuantity(cartItem.getQuantity() + addCartItemDto.getQuantity());
            return cartItemService.updateCartItem(cartItem);
        }

        return cartItemService.addCartItem(addCartItemDto);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity deleteCartItem(@IfLogin LoginUserDto loginUserDto, @PathVariable Long cartItemId) {
        if(cartItemService.isCartItemExist(loginUserDto.getMemberId(), cartItemId) == false)
            return ResponseEntity.badRequest().build();
        cartItemService.deleteCartItem(loginUserDto.getMemberId(), cartItemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<CartItem> getCartItems(@IfLogin LoginUserDto loginUserDto, @RequestParam(required = false) Long cartId) {
        if(cartId == null) {
            return cartItemService.getCartItems(loginUserDto.getMemberId());
        }
        return cartItemService.getCartItems(loginUserDto.getMemberId(), cartId);
    }
}
