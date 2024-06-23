package com.example.store.controller;

import com.example.store.entity.Cart;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final MemberService memberService;

    @PostMapping
    public Cart addCart(@IfLogin LoginUserDto loginUserDto) {
        log.info("loginUserDto: {}", loginUserDto);
        Optional<Member> member = memberService.getMember(loginUserDto.getEmail());
/*        if(member.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자가 없습니다.");
        }*/
        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());
        Cart cart = cartService.addCart(member.get().getMemberId(), date);
        return cart;
    }
}
