package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import com.example.store.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;
    private final CartService cartService;

    @GetMapping("/info")
    public ResponseDto<ResponseMemberDto> userInfo(@IfLogin LoginUserDto loginUserDto) {
        return memberService.userInfo(loginUserDto);

    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessDto> signup(@RequestBody @Valid MemberSignupDto memberSignupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return memberService.createMember(memberSignupDto);
    }

    @PostMapping("/login")
    public ResponseDto<MemberLoginResponseDto> login(@RequestBody @Valid MemberLoginDto loginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException();
        }

        return memberService.login(loginDto);
    }

    @PostMapping("/refreshToken")
    public ResponseDto<MemberLoginResponseDto> requestRefresh(@RequestBody RefreshTokenDto refreshTokenDto) {
        return memberService.refreshToken(refreshTokenDto);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<SuccessDto> logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        refreshTokenService.deleteRefreshToken(refreshTokenDto.getRefreshToken());

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @DeleteMapping("/signout")
    public ResponseEntity<SuccessDto> signout(@IfLogin LoginUserDto loginUserDto) {
        return memberService.signout(loginUserDto);
    }
}
