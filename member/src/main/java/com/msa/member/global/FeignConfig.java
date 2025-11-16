package com.msa.member.global;

import feign.Client;
import feign.Logger;
import feign.codec.ErrorDecoder;
import feign.httpclient.ApacheHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Feign 로그 레벨 설정
    }

    /**
     * PATCH 메서드 지원을 위한 Apache HttpClient 설정
     */
    @Bean
    public Client feignClient() {
        return new ApacheHttpClient();
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
                    return new RuntimeException("Resource not found");
                case 500:
                    return new RuntimeException("Service internal error");
                default:
                    return new RuntimeException("Service error: " + response.status());
            }
        }
    }
}
