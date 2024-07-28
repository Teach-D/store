package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.entity.Cart;
import com.example.store.entity.CartItem;
import com.example.store.entity.Member;
import com.example.store.entity.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartItemService;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import com.example.store.service.ProductService;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/cartItems")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;
    private final ProductService productService;
    private final MemberService memberService;
    private final CartService cartService;

    @GetMapping
    public ResponseDto<List<ResponseCartItemDto>> getCartItems(@IfLogin LoginUserDto loginUserDto) {
        return cartItemService.getCartItems(loginUserDto);
    }

    @GetMapping("/{cartItemId}")
    public ResponseDto<ResponseCartItemDto> getCartItem(@PathVariable Long cartItemId) {
        return cartItemService.getCartItem(cartItemId);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<SuccessDto> addCartItem(@IfLogin LoginUserDto loginUserDto, @RequestBody AddCartItemDto addCartItemDto, @PathVariable Long productId) {
        return cartItemService.addCartItem(loginUserDto, addCartItemDto, productId);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<SuccessDto> editCartItem(@PathVariable Long cartItemId, @RequestBody EditCartItemDto editCartItemDto) {
        return cartItemService.editCartItem(cartItemId, editCartItemDto);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<SuccessDto> deleteCartItem(@IfLogin LoginUserDto loginUserDto, @PathVariable Long cartItemId) {
        return cartItemService.deleteCartItem(loginUserDto, cartItemId);
    }
}