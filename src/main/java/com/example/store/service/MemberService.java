package com.example.store.service;

import com.example.store.dto.request.RefreshTokenDto;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.dto.response.*;
import com.example.store.entity.*;
import com.example.store.exception.MemberException1;
import com.example.store.exception.ex.ErrorCode;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.jwt.util.JwtTokenizer;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CartRepository;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtTokenizer jwtTokenizer;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ResponseEntity<SuccessDto> createMember(RequestSignUp requestSignUp) {

        if (memberRepository.existsByEmail(requestSignUp.getEmail())) {
            throw new MemberException1(ErrorCode.DUPLICATE_EMAIL, "이메일이 중복됨 %s".formatted(requestSignUp.getEmail()));
        }

        Member member = Member.builder()
                .name(requestSignUp.getName())
                .email(requestSignUp.getEmail())
                .password(passwordEncoder.encode(requestSignUp.getPassword()))
                .build();

        member.addRole(Role.USER);
        Member saveMember = memberRepository.save(member);

        ResponseSignUp responseSignUp = ResponseSignUp.builder()
                .memberId(saveMember.getId())
                .email(saveMember.getEmail())
                .name(saveMember.getName())
                .regDate(saveMember.getRegDate())
                .build();

        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Cart newCart = Cart.builder().member(saveMember).date(date).build();
        cartRepository.save(newCart);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

/*    @Transactional
    public ResponseEntity login(RequestSignIn loginDto) {

        // email이 없을 경우 Exception이 발생한다. Global Exception에 대한 처리가 필요하다.
        Member member = memberRepository.findByEmail(loginDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        if(!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())){
            throw new NotFoundMemberException();
        }
        // List<Role> ===> List<String>
        List<String> roles = new ArrayList<>();
        Role role = member.getRole();
        roles.add(String.valueOf(role));

        // JWT토큰을 생성하였다. jwt라이브러리를 이용하여 생성.
        String accessToken = jwtTokenizer.createAccessToken(member.getMemberId(), member.getEmail(), roles);
        String refreshToken = jwtTokenizer.createRefreshToken(member.getMemberId(), member.getEmail(),  roles);

        // RefreshToken을 DB에 저장한다. 성능 때문에 DB가 아니라 Redis에 저장하는 것이 좋다.
        RefreshToken refreshTokenEntity = RefreshToken.builder().tokenName(refreshToken).memberId(member.getMemberId()).build();

        RefreshToken save = refreshTokenRepository.save(refreshTokenEntity);

        ResponseSignIn loginResponse = ResponseSignIn.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getMemberId())
                .nickname(member.getName())
                .build();

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }*/
    @Transactional
    public ResponseDto<ResponseSignIn> login(RequestSignIn loginDto) {

        // email이 없을 경우 Exception이 발생한다. Global Exception에 대한 처리가 필요하다.
        Member member = memberRepository.findByEmail(loginDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        if(!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())){
            throw new NotFoundMemberException();
        }
        // List<Role> ===> List<String>
        List<String> roles = new ArrayList<>();
        Role role = member.getRole();
        roles.add(String.valueOf(role));

        // JWT토큰을 생성하였다. jwt라이브러리를 이용하여 생성.
        String accessToken = jwtTokenizer.createAccessToken(member.getId(), member.getEmail(), roles);
        String refreshToken = jwtTokenizer.createRefreshToken(member.getId(), member.getEmail(),  roles);

        // RefreshToken을 DB에 저장한다. 성능 때문에 DB가 아니라 Redis에 저장하는 것이 좋다.
        RefreshToken refreshTokenEntity = RefreshToken.builder().tokenName(refreshToken).memberId(member.getId()).build();

        RefreshToken save = refreshTokenRepository.save(refreshTokenEntity);

        ResponseSignIn loginResponse = ResponseSignIn.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .nickname(member.getName())
                .build();

        return ResponseDto.success(loginResponse);
    }

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
    }

    public ResponseDto<ResponseMember> userInfo(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        ResponseMember responseMember = ResponseMember.builder()
                .email(member.getEmail())
                .name(member.getName())
                .regDate(member.getRegDate())
                .role(member.getRole())
                .build();
        log.info("a" + responseMember.getEmail());

        return ResponseDto.success(responseMember);
    }

    public ResponseEntity<SuccessDto> signout(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);

        for (CartItem cartItem : cart.getCartItemList()) {
            Product product = cartItem.getProduct();
            product.updateQuantity(product.getQuantity() + cartItem.getQuantity());
        }

        cartRepository.delete(cart);
        memberRepository.delete(member);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }


    @Transactional(readOnly = true)
    public Optional<Member> getMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMember(String email) {
        return memberRepository.findByEmail(email);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }



}
