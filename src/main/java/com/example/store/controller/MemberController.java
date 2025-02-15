package com.example.store.controller;

import com.example.store.dto.request.RefreshTokenDto;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseMember;
import com.example.store.dto.response.ResponseSignIn;
import com.example.store.dto.response.SuccessDto;
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

import java.util.List;

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
    public ResponseDto<ResponseMember> userInfo(@IfLogin LoginUserDto loginUserDto) {
        return memberService.userInfo(loginUserDto);
    }

/*    @GetMapping
    public ResponseDto<List<ResponseMember>> getAllUser() {
        return memberService.userInfo(loginUserDto);
    }*/

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Valid RequestSignUp requestSignUp, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return memberService.createMember(requestSignUp);
    }

/*    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid RequestSignIn loginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return memberService.login(loginDto);
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid RequestSignIn loginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message: ").append(message).append(" ");
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        ResponseDto<ResponseSignIn> response = memberService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseDto<ResponseSignIn> requestRefresh(@RequestBody RefreshTokenDto refreshTokenDto) {
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
