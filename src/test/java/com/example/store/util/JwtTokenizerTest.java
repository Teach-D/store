package com.example.store.util;

import com.example.store.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
public class JwtTokenizerTest {

    @Autowired
    JwtTokenizer jwtTokenizer;

    @Value("${jwt.secretKey}")
    String accessSecret;

    public final Long ACCESS_TOKEN_EXPIRE_COUNT = 30 * 60 * 1000L;

    @Test
    public void createToken() {
        String email = "wkadht0619@gmail.com";
        List<String> roles = List.of("ROLE_USER");
        Long id = 1L;

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", roles);
        claims.put("userId", id);

        byte[] accessSecret = this.accessSecret.getBytes(StandardCharsets.UTF_8);

        String JwtToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + this.ACCESS_TOKEN_EXPIRE_COUNT)) // 현재시간으로부터 30분뒤에 만료.
                .signWith(Keys.hmacShaKeyFor(accessSecret))
                .compact();

        log.info("createToken : " + JwtToken);
    }

    @Test
    public void parseToken() {
        byte[] accessSecret = this.accessSecret.getBytes(StandardCharsets.UTF_8);
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6IndrYWRodDA2MTlAZ21haWwuY29tIn0.aXcl6jabz7ZH10aqnQ6Unwe1EhdJsxCMs25_qWqwxSU";

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(accessSecret))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        log.info("parseToken : " + claims.getSubject());
    }
}









