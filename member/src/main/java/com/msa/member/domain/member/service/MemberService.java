package com.msa.member.domain.member.service;

import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.cart.repository.CartRepository;
import com.msa.member.domain.member.dto.request.RequestSignIn;
import com.msa.member.domain.member.dto.request.RequestSignUp;
import com.msa.member.domain.member.dto.response.ResponseMemberDto;
import com.msa.member.domain.member.dto.response.ResponseSignIn;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.entity.Role;
import com.msa.member.domain.member.repository.MemberRepository;
import com.msa.member.global.exception.CustomException;
import com.msa.member.global.exception.ErrorCode;
import com.msa.member.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.msa.member.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void createMember(RequestSignUp requestSignUp) {

        Member member = Member.builder()
                .name(requestSignUp.getName())
                .email(requestSignUp.getEmail())
                .password(passwordEncoder.encode(requestSignUp.getPassword()))
                .build();

        member.addRole(Role.ROLE_USER);
        Member saveMember = memberRepository.save(member);


        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Cart newCart = Cart.builder().member(saveMember).date(date).build();
        cartRepository.save(newCart);
    }

    @Transactional
    public ResponseSignIn login(RequestSignIn loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        if(!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())){
            throw new CustomException(MEMBER_NOT_FOUND);
        }
        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getName(), String.valueOf(member.getRole()));

        return ResponseSignIn.builder()
                .nickname(member.getName())
                .accessToken(accessToken)
                .build();
    }

/*
    public ResponseDto<ResponseSignIn> refreshToken(RefreshTokenDto refreshTokenDto) {

        log.info(String.valueOf(refreshTokenDto.getRefreshToken()));
        RefreshToken refreshToken = refreshTokenRepository.findByTokenName(refreshTokenDto.getRefreshToken()).orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        log.info(String.valueOf(refreshToken));
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken.getTokenName());

        Long userId = Long.valueOf((Integer) claims.get("userId"));

        Member member = memberRepository.findById(userId).orElseThrow(NotFoundMemberException::new);

        List roles = (List) claims.get("roles");
        String email = claims.getSubject();

        String accessToken = jwtTokenizer.createAccessToken(userId, email, roles);

        ResponseSignIn loginResponse = ResponseSignIn.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getRefreshToken())
                .nickname(member.getName())
                .memberId(member.getId())
                .build();

        return ResponseDto.success(loginResponse);
    }*/

    public ResponseMemberDto userInfo(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        return ResponseMemberDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .regDate(member.getRegDate())
                .role(member.getRole())
                .build();
    }

/*    public ResponseEntity<SuccessDto> signout(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);

        for (CartItem cartItem : cart.getCartItemList()) {
            Product product = cartItem.getProduct();
            product.updateQuantity(product.getQuantity() + cartItem.getQuantity());
        }

        cartRepository.delete(cart);
        memberRepository.delete(member);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }*/

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }


    @Transactional(readOnly = true)
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMember(String email) {
        return memberRepository.findByEmail(email);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }



}
