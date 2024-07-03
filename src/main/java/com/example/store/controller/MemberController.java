package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.entity.*;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.JwtTokenizer;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CartService;
import com.example.store.service.MemberService;
import com.example.store.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/members")
public class MemberController {

    private final JwtTokenizer jwtTokenizer;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid MemberSignupDto memberSignupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Member member = Member.builder()
                        .name(memberSignupDto.getName())
                        .email(memberSignupDto.getEmail())
                        .password(memberSignupDto.getPassword())
                        .build();

        Member saveMember = memberService.addMember(member);

        MemberSignupResponseDto memberSignupResponseDto = MemberSignupResponseDto.builder()
                        .memberId(saveMember.getMemberId())
                        .email(saveMember.getEmail())
                        .name(saveMember.getName())
                        .regDate(saveMember.getRegDate())
                        .build();

        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());
        Cart cart = cartService.addCart(member.getMemberId(), date);

        return new ResponseEntity<>(memberSignupResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid MemberLoginDto loginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // email이 없을 경우 Exception이 발생한다. Global Exception에 대한 처리가 필요하다.
        Member member = memberService.findByEmail(loginDto.getEmail());
        if(!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        // List<Role> ===> List<String>
        List<String> roles = member.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        // JWT토큰을 생성하였다. jwt라이브러리를 이용하여 생성.
        String accessToken = jwtTokenizer.createAccessToken(member.getMemberId(), member.getEmail(), roles);
        String refreshToken = jwtTokenizer.createRefreshToken(member.getMemberId(), member.getEmail(),  roles);

        // RefreshToken을 DB에 저장한다. 성능 때문에 DB가 아니라 Redis에 저장하는 것이 좋다.
        RefreshToken refreshTokenEntity = RefreshToken.builder().value(refreshToken).memberId(member.getMemberId()).build();
        refreshTokenService.addRefreshToken(refreshTokenEntity);

        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getMemberId())
                .nickname(member.getName())
                .build();
        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }
    @DeleteMapping("/logout")
    public ResponseEntity logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        log.info("logout");
        log.info(String.valueOf(refreshTokenDto));
        refreshTokenService.deleteRefreshToken(refreshTokenDto.getRefreshToken());
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity requestRefresh(@RequestBody RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken = refreshTokenService.findRefreshToken(refreshTokenDto.getRefreshToken()).orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken.getValue());

        Long userId = Long.valueOf((Integer) claims.get("userId"));

        Member member = memberService.getMember(userId).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List roles = (List) claims.get("roles");
        String email = claims.getSubject();

        String accessToken = jwtTokenizer.createAccessToken(userId, email, roles);

        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getRefreshToken())
                .nickname(member.getName())
                .memberId(member.getMemberId())
                .build();

        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }

    @GetMapping("/info")
    public ResponseEntity userinfo(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        log.info(member.getEmail());
        ResponseMemberDto responseMemberDto = ResponseMemberDto.builder()
                        .email(member.getEmail())
                        .name(member.getName())
                        .regDate(member.getRegDate())
                        .roles(member.getRoles())
                        .build();

        return new ResponseEntity(responseMemberDto, HttpStatus.OK);
    }

    @DeleteMapping("/signout")
    public ResponseEntity signout(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Cart cart = cartService.getCart(member.getMemberId());
        for (CartItem cartItem : cart.getCartItemList()) {
            Product product = cartItem.getProduct();
            product.updateQuantity(product.getQuantity() + cartItem.getQuantity());
        }

        cartService.deleteCart(cart.getId());
        memberService.deleteMember(member.getMemberId());
        return new ResponseEntity(HttpStatus.OK);
    }
}
