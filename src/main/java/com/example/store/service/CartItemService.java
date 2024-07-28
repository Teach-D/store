package com.example.store.service;

import com.example.store.dto.*;
import com.example.store.entity.Cart;
import com.example.store.entity.CartItem;
import com.example.store.entity.Member;
import com.example.store.entity.Product;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.NotFoundCartItemException;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.CartRepository;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ResponseDto<ResponseCartItemDto> getCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);
        ResponseCartItemDto responseCartItemDto = ResponseCartItemDto.builder().quantity(cartItem.getQuantity()).product(cartItem.getProduct()).build();

        return ResponseDto.success(responseCartItemDto);
    }

    public ResponseDto<List<ResponseCartItemDto>> getCartItems(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<ResponseCartItemDto> cartItemDtos = new ArrayList<>();

        cartItems.forEach(cartItem -> {
            ResponseCartItemDto responseCartItemDto = ResponseCartItemDto
                    .builder().quantity(cartItem.getQuantity()).product(cartItem.getProduct()).id(cartItem.getId()).build();
            cartItemDtos.add(responseCartItemDto);
        });

        return ResponseDto.success(cartItemDtos);

    }

    public ResponseEntity<SuccessDto> addCartItem(LoginUserDto loginUserDto, AddCartItemDto addCartItemDto, Long productId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Product product = productRepository.findById(productId).orElseThrow(NotFoundProductException::new);

        if (cartItemRepository.existsByCartIdAndProductId(addCartItemDto.getCartId(), productId)) {
            CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(addCartItemDto.getCartId(), productId).orElseThrow(NotFoundCartItemException::new);
            cartItem.updateQuantity(cartItem.getQuantity() + addCartItemDto.getQuantity());
            product.updateQuantity(product.getQuantity() - addCartItemDto.getQuantity());
            return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
            //updateCartItem(cartItem);
        }

        product.updateQuantity(product.getQuantity() - addCartItemDto.getQuantity());

        Cart cart = cartRepository.findById(addCartItemDto.getCartId()).orElseThrow(NotFoundCartException::new);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .quantity(addCartItemDto.getQuantity())
                .product(product)
                .build();

        CartItem save = cartItemRepository.save(cartItem);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> editCartItem(Long cartItemId, EditCartItemDto editCartItemDto) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);
        cartItem.updateQuantity(editCartItemDto.getQuantity());

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteCartItem(LoginUserDto loginUserDto, Long cartItemId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(NotFoundCartItemException::new);

        Product product = productRepository.findById(cartItem.getProduct().getId()).orElseThrow(NotFoundProductException::new);

        if (isCartItemExistByCartId(cart.getId(), cartItemId) == false)
            return ResponseEntity.badRequest().build();

        product.updateQuantity(product.getQuantity() + cartItem.getQuantity());
        cartItemRepository.deleteById(cartItemId);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findCartItemByCartId(cartId);
    }

    public boolean isCartItemExistByCartId(Long id, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        return (cartItem.getCart().getId() == id);
    }
}
