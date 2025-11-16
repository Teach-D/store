package com.msa.member.domain.cart.controller;

import com.msa.member.domain.cart.dto.response.ResponseCartDto;
import com.msa.member.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "장바구니 조회",
            description = "회원가입 합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "장바구니 조회 성공"
    )
    public ResponseEntity<Long> findCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(CREATED).body(cartService.findCart(userId));
    }

    @GetMapping("/{memberId}")
    public Long getCartId(@PathVariable Long memberId) {
        return cartService.findCart(memberId);
    }
}
