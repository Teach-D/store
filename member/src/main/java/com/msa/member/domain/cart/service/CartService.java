package com.msa.member.domain.cart.service;

import com.msa.member.domain.cart.dto.response.ResponseCartDto;
import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.cart.entity.CartItem;
import com.msa.member.domain.cart.repository.CartItemRepository;
import com.msa.member.domain.cart.repository.CartRepository;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.repository.MemberRepository;
import com.msa.member.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.msa.member.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;

    public Long findCart(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Cart cart = cartRepository.findByMember(member).orElseThrow(() -> new CustomException(CART_NOT_FOUND));

        return cart.getId();
    }

    public void deleteCartItemsByIds(Cart cart) {
        for (CartItem cartItem : cart.getCartItemList()) {
            cartItemRepository.deleteById(cartItem.getId());
            log.info("cartItemId : {}", cartItem.getId());
        }
    }
}
