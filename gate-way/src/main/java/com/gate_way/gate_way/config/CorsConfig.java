package com.gate_way.gate_way.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // 허용할 Origin (프론트엔드 주소)
        corsConfig.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "*"  // 개발 단계에서는 모두 허용, 프로덕션에서는 구체적인 도메인 설정
        ));
        
        // 허용할 HTTP 메서드
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 허용할 헤더
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // 노출할 헤더
        corsConfig.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-User-Id",
            "X-User-Name",
            "X-User-Role"
        ));
        
        // 인증 정보 허용 (Cookie, Authorization 헤더)
        corsConfig.setAllowCredentials(false);  // allowedOrigins에 *를 사용할 경우 false여야 함
        
        // preflight 요청 캐시 시간 (초)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
