package com.gate_way.gate_way.config;

import com.gate_way.gate_way.filter.JwtAuthenticationFilter;
import com.gate_way.gate_way.filter.RoleAuthorizationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * API Gateway Route Configuration for EKS
 * 
 * 보안 정책:
 * - User Service: 회원가입/로그인 public, 나머지 인증 필요
 * - Product Service: GET public, POST/PUT/DELETE 인증 필요
 * - Order Service: 모든 요청 ADMIN만 접근 가능
 */
@Slf4j
@Configuration
public class RouteConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleAuthorizationFilter roleAuthorizationFilter;
    
    @Value("${services.member.url:http://member-service:8080}")
    private String memberServiceUrl;
    
    @Value("${services.order.url:http://order-service:8080}")
    private String orderServiceUrl;
    
    @Value("${services.product.url:http://product-service:8080}")
    private String productServiceUrl;

    public RouteConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                      RoleAuthorizationFilter roleAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.roleAuthorizationFilter = roleAuthorizationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Initializing Gateway Routes with Service URLs:");
        log.info("Member Service: {}", memberServiceUrl);
        log.info("Order Service: {}", orderServiceUrl);
        log.info("Product Service: {}", productServiceUrl);

        return builder.routes()
            // ========== Member Service ==========
            .route("member-public", r -> r
                .path("/members/signup", "/members/login", "/actuator/**")
                .filters(f -> f.stripPrefix(0))
                .uri(memberServiceUrl))
            .route("member-public", r -> r
                    .path("/members/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(memberServiceUrl))

            .route("cart-public", r -> r
                .path("/carts/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(memberServiceUrl))

            .route("cart-item-public", r -> r
                .path("/cartItems/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(memberServiceUrl))

            .route("coupon-public", r -> r
                .path("/coupons/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(memberServiceUrl))

            .route("delivery-public", r -> r
                .path("/deliveries/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(memberServiceUrl))

            // ========== Order Service ==========
            .route("order-routes", r -> r
                .path("/orders/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))
                    .uri(orderServiceUrl))

            // ========== Product Service ==========
            .route("product-routes", r -> r
                .path("/products/**")
                .filters(f -> f.stripPrefix(0))
                .uri(productServiceUrl))

            .route("category-routes", r -> r
                .path("/categories/**")
                .filters(f -> f.stripPrefix(0))
                .uri(productServiceUrl))

            .route("review-routes", r -> r
                .path("/review/**")
                    .filters(f -> f
                            .filter(jwtAuthenticationFilter.apply(
                                    new JwtAuthenticationFilter.Config()))
                            .stripPrefix(0))                .uri(productServiceUrl))

            .route("tag-routes", r -> r
                .path("/tags/**")
                .filters(f -> f.stripPrefix(0))
                .uri(productServiceUrl))

            .build();
    }
    
    /**
     * RoleAuthorizationFilter.Config 생성 헬퍼 메서드
     * 
     * @param role 필요한 Role (예: "ADMIN", "USER" 또는 "ADMIN,MANAGER")
     * @return RoleAuthorizationFilter.Config
     */
    private RoleAuthorizationFilter.Config createRoleConfig(String role) {
        RoleAuthorizationFilter.Config config = new RoleAuthorizationFilter.Config();
        config.setRequiredRole(role);
        return config;
    }
}
