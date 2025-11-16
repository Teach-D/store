package com.gate_way.gate_way.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT에서 사용자 정보 추출
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * JWT에서 userId 추출 (subject에 저장된 ID)
     */
    public Long getUserId(String token) {
        String subject = getClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    /**
     * JWT에서 username 추출
     */
    public String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * JWT에서 role 추출
     */
    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }
}
