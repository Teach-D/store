package com.example.store.service;

import com.example.store.dto.response.ResponseCart;
import com.example.store.dto.response.ResponseDto;
import com.example.store.entity.Cart;
import com.example.store.entity.Member;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CartRepository;
import com.example.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    public void deleteCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public ResponseDto<ResponseCart> getCart(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);

        ResponseCart responseCart = ResponseCart.builder().id(cart.getId()).build();

        return ResponseDto.success(responseCart);
    }
}
