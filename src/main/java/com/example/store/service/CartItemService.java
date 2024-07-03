package com.example.store.service;

import com.example.store.dto.AddCartItemDto;
import com.example.store.entity.Cart;
import com.example.store.entity.CartItem;
import com.example.store.entity.Product;
import com.example.store.repository.CartItemRepository;
import com.example.store.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @Transactional
    public CartItem addCartItem(AddCartItemDto addCartItemDto, Product product) {
        Cart cart = cartRepository.findById(addCartItemDto.getCartId()).orElseThrow();

        CartItem cartItem = CartItem.builder()
                        .cart(cart)
                        .quantity(addCartItemDto.getQuantity())
                        .product(product)
                        .build();

        return cartItemRepository.save(cartItem);
    }

    @Transactional(readOnly = true)
    public CartItem getCartItem(Long cartId, Long productId) {
        return cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId).orElseThrow();
    }

    @Transactional
    public CartItem updateCartItem(CartItem cartItem) {
        CartItem findCartItem = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        findCartItem.updateQuantity(cartItem.getQuantity());
        return findCartItem;
    }

    @Transactional(readOnly = true)
    public boolean isCartItemExist(Long cartId, Long productId) {
        return cartItemRepository.existsByCartIdAndProductId(cartId, productId);
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        cartItemRepository.deleteByCart_memberIdAndId(memberId, cartItemId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findCartItemByCartId(cartId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long memberId) {
        return cartItemRepository.findByCart_memberId(memberId);
    }

    public boolean isCartItemExistByCartId(Long id, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        return (cartItem.getCart().getId() == id);
    }
}
