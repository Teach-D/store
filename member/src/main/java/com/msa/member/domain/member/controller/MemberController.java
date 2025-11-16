package com.msa.member.domain.member.controller;

import com.msa.member.domain.cart.service.CartService;
import com.msa.member.domain.member.dto.request.RefreshTokenDto;
import com.msa.member.domain.member.dto.request.RequestSignIn;
import com.msa.member.domain.member.dto.request.RequestSignUp;
import com.msa.member.domain.member.dto.response.ResponseMemberDto;
import com.msa.member.domain.member.dto.response.ResponseSignIn;
import com.msa.member.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final CartService cartService;

    @GetMapping("/info")
    public ResponseEntity<ResponseMemberDto> userInfo(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.userInfo(userId));
    }

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

        memberService.createMember(requestSignUp);
        return ResponseEntity.status(CREATED).build();
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

        ResponseSignIn response = memberService.login(loginDto);
        return ResponseEntity.ok(response);
    }

/*    @PostMapping("/refreshToken")
    public ResponseDto<ResponseSignIn> requestRefresh(@RequestBody RefreshTokenDto refreshTokenDto) {
        return memberService.refreshToken(refreshTokenDto);
    }*/

/*    @DeleteMapping("/logout")
    public ResponseEntity<SuccessDto> logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        refreshTokenService.deleteRefreshToken(refreshTokenDto.getRefreshToken());

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }*/

/*    @DeleteMapping("/signout")
    public ResponseEntity<SuccessDto> signout(@IfLogin LoginUserDto loginUserDto) {
        return memberService.signout(loginUserDto);
    }*/
}
