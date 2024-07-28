package com.example.store.service;

import com.example.store.dto.*;
import com.example.store.entity.*;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.jwt.util.JwtTokenizer;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CartRepository;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.RefreshTokenRepository;
import com.example.store.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtTokenizer jwtTokenizer;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ResponseEntity<SuccessDto> createMember(MemberSignupDto memberSignupDto) {

        Member member = Member.builder()
                .name(memberSignupDto.getName())
                .email(memberSignupDto.getEmail())
                .password(passwordEncoder.encode(memberSignupDto.getPassword()))
                .build();

        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        member.addRole(userRole.get());
        Member saveMember = memberRepository.save(member);

        MemberSignupResponseDto memberSignupResponseDto = MemberSignupResponseDto.builder()
                .memberId(saveMember.getMemberId())
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

    public ResponseDto<MemberLoginResponseDto> login(MemberLoginDto loginDto) {

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
        RefreshToken refreshTokenEntity = RefreshToken.builder().value(refreshToken).memberId(member.getMemberId()).build();
        refreshTokenRepository.save(refreshTokenEntity);

        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getMemberId())
                .nickname(member.getName())
                .build();

        return ResponseDto.success(loginResponse);
    }

    public ResponseDto<MemberLoginResponseDto> refreshToken(RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken = refreshTokenRepository.findByValue(refreshTokenDto.getRefreshToken()).orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken.getValue());

        Long userId = Long.valueOf((Integer) claims.get("userId"));

        Member member = memberRepository.findById(userId).orElseThrow(NotFoundMemberException::new);

        List roles = (List) claims.get("roles");
        String email = claims.getSubject();

        String accessToken = jwtTokenizer.createAccessToken(userId, email, roles);

        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getRefreshToken())
                .nickname(member.getName())
                .memberId(member.getMemberId())
                .build();

        return ResponseDto.success(loginResponse);
    }

    public ResponseDto<ResponseMemberDto> userInfo(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        ResponseMemberDto responseMemberDto = ResponseMemberDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .regDate(member.getRegDate())
                .role(member.getRole())
                .build();
        log.info("a" + responseMemberDto.getEmail());

        return ResponseDto.success(responseMemberDto);
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
