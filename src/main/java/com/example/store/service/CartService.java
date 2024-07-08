package com.example.store.service;

import com.example.store.entity.Cart;
import com.example.store.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    public Cart addCart(Long memberId, String date) {
        Optional<Cart> cart = cartRepository.findByMemberIdAndDate(memberId, date);
        if(cart.isEmpty()) {
            Cart newCart = Cart.builder().memberId(memberId).date(date).build();
            Cart saveCart = cartRepository.save(newCart);
            return saveCart;
        } else {
            return cart.get();
        }
    }

    public Cart getCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).get();
        return cart;
    }

    public void deleteCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}
