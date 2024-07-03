package com.example.store.controller;

import com.example.store.dto.AddCartItemDto;
import com.example.store.dto.EditCartItemDto;
import com.example.store.dto.ResponseCartItemDto;
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

    @PostMapping("/{productId}")
    public void addCartItem(@IfLogin LoginUserDto loginUserDto, @RequestBody AddCartItemDto addCartItemDto, @PathVariable Long productId) {
        log.info(loginUserDto.getEmail());
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        if (cartItemService.isCartItemExist(addCartItemDto.getCartId(), productId)) {
            log.info(" 중복 ");
            CartItem cartItem = cartItemService.getCartItem(addCartItemDto.getCartId(), productId);
            cartItem.updateQuantity(cartItem.getQuantity() + addCartItemDto.getQuantity());
            cartItemService.updateCartItem(cartItem);
            return;
        }

        Product product = productService.getProduct(productId);
        product.updateQuantity(product.getQuantity() - addCartItemDto.getQuantity());
        cartItemService.addCartItem(addCartItemDto, product);
    }

    @PutMapping("/{cartItemId}")
    public void editCartItem(@PathVariable Long cartItemId, @RequestBody EditCartItemDto editCartItemDto) {
        CartItem cartItem = cartItemService.getCartItem(cartItemId);
        cartItem.updateQuantity(editCartItemDto.getQuantity());
        log.info(editCartItemDto.toString());
        cartItemService.updateCartItem(cartItem);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity deleteCartItem(@IfLogin LoginUserDto loginUserDto, @PathVariable Long cartItemId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Cart cart = cartService.getCart(member.getMemberId());
        CartItem cartItem = cartItemService.getCartItem(cartItemId);
        Product product = cartItem.getProduct();
        Product product1 = productService.getProduct(product.getId());

        if (cartItemService.isCartItemExistByCartId(cart.getId(), cartItemId) == false)
            return ResponseEntity.badRequest().build();
        product1.updateQuantity(product1.getQuantity() + cartItem.getQuantity());
        cartItemService.deleteCartItem(member.getMemberId(), cartItemId);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<ResponseCartItemDto> getCartItems(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<CartItem> cartItems = cartItemService.getCartItems(member.getMemberId());
        List<ResponseCartItemDto> cartItemDtos = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ResponseCartItemDto responseCartItemDto = ResponseCartItemDto
                    .builder().quantity(cartItem.getQuantity()).product(cartItem.getProduct()).build();
            cartItemDtos.add(responseCartItemDto);
        }

        return cartItemDtos;
    }

    @GetMapping("/{cartItemId}")
    public ResponseCartItemDto getCartItem(@PathVariable Long cartItemId) {
        CartItem cartItem = cartItemService.getCartItem(cartItemId);
        ResponseCartItemDto responseCartItemDto = ResponseCartItemDto.builder().quantity(cartItem.getQuantity()).product(cartItem.getProduct()).build();

        return responseCartItemDto;
    }
}