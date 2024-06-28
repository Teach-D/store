package com.example.store.controller;

import com.example.store.dto.ResponseCartDto;
import com.example.store.entity.Cart;
import com.example.store.entity.CartItem;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final MemberService memberService;

    @GetMapping
    public ResponseCartDto getCart(@IfLogin LoginUserDto loginUserDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Cart cart = cartService.getCart(member.getMemberId());
        ResponseCartDto responseCartDto = new ResponseCartDto();
        responseCartDto.setId(cart.getId());
        return responseCartDto;
    }

}
