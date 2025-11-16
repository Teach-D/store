package com.gate_way.gate_way.filter;

import com.gate_way.gate_way.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway에서 Role 기반 접근 제어를 수행하는 필터
 * JWT 토큰에서 추출한 Role을 검증하여 특정 경로에 대한 접근을 제어
 */
@Slf4j
@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public RoleAuthorizationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = extractToken(exchange);
            
            log.info("=== Role Authorization Filter ===");
            log.info("Request Path: {}", exchange.getRequest().getPath());
            log.info("Required Role: {}", config.getRequiredRole());
            
            if (token == null) {
                log.warn("❌ No token found for role authorization");
                return onError(exchange, "No authentication token", HttpStatus.UNAUTHORIZED);
            }

            try {
                // JWT에서 Role 추출
                String userRole = jwtTokenProvider.getRole(token);
                
                log.info("User Role: {}", userRole);
                
                // Role 검증
                if (!hasRequiredRole(userRole, config.getRequiredRole())) {
                    log.warn("❌ Access denied - Required role: {}, User role: {}", 
                            config.getRequiredRole(), userRole);
                    return onError(exchange, 
                            "Access denied. Required role: " + config.getRequiredRole(),
                            HttpStatus.FORBIDDEN);
                }
                
                log.info("✅ Role authorization success - {} access granted", userRole);
                return chain.filter(exchange);
                
            } catch (Exception e) {
                log.error("❌ Role authorization failed: {}", e.getMessage());
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 사용자 Role이 필요한 Role과 일치하는지 검증
     * 
     * @param userRole 사용자의 Role
     * @param requiredRole 필요한 Role (쉼표로 구분하여 여러 Role 지정 가능)
     * @return 권한이 있으면 true, 없으면 false
     */
    private boolean hasRequiredRole(String userRole, String requiredRole) {
        if (userRole == null || requiredRole == null) {
            return false;
        }
        
        // "ROLE_" 접두사 제거 (있을 경우)
        userRole = userRole.replace("ROLE_", "");
        
        // 여러 Role이 허용되는 경우 (예: "ADMIN,MANAGER")
        String[] allowedRoles = requiredRole.split(",");
        for (String allowed : allowedRoles) {
            allowed = allowed.trim().replace("ROLE_", "");
            if (userRole.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 에러 응답 반환
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        
        log.error("Role authorization error: {} (Status: {})", message, status);
        
        // JSON 형식의 에러 메시지 반환
        String errorJson = String.format(
            "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
            status.getReasonPhrase(),
            message,
            java.time.Instant.now().toString()
        );
        
        response.getHeaders().add("Content-Type", "application/json");
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(errorJson.getBytes()))
        );
    }

    /**
     * Filter 설정 클래스
     */
    public static class Config {
        private String requiredRole;

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }
    }
}
