package com.gate_way.gate_way.filter;

import com.gate_way.gate_way.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            log.info("=== JWT Authentication Filter ===");
            log.info("Request Path: {}", request.getPath());
            log.info("Request Method: {}", request.getMethod());
            
            // Authorization 헤더 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.error("No authorization header found");
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String token = authorizationHeader.replace("Bearer ", "");

            log.info("Token extracted: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

            // JWT 검증
            if (!jwtTokenProvider.validateToken(token)) {
                log.error("Invalid JWT token");
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            try {
                // JWT에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserId(token);
                String username = jwtTokenProvider.getUsername(token);
                String role = jwtTokenProvider.getRole(token);

                log.info("JWT Validation Success - userId: {}, username: {}, role: {}", userId, username, role);

                // 사용자 정보를 헤더에 추가
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

                log.info("User info headers added successfully");

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Error extracting user info from JWT: {}", e.getMessage());
                return onError(exchange, "Error processing JWT", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Authentication error: {}", error);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
