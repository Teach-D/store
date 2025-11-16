package com.msa.product.global;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Feign 로그 레벨 설정
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            switch (response.status()) {
                case 404:
                    return new RuntimeException("User not found");
                case 500:
                    return new RuntimeException("User service internal error");
                default:
                    return new RuntimeException("User service error: " + response.status());
            }
        }
    }
}
