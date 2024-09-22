package com.example.store.controller;

import com.example.store.dto.response.ResponseCart;
import com.example.store.dto.response.ResponseDto;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final MemberService memberService;

    @GetMapping
    @Operation(
            summary = "장바구니 조회",
            description = "회원가입 합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "장바구니 조회 성공"
    )
    public ResponseDto<ResponseCart> getCart(@IfLogin LoginUserDto loginUserDto) {
        return cartService.getCart(loginUserDto);
    }
}
