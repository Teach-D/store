package com.msa.order.global.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException ex) {
        log.warn("CustomException 발생: {}", ex.getErrorCode().getMessage());
        return ErrorResponseEntity.toResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Map<Object, Object>> handleCallNotPermittedException(CallNotPermittedException e) {
         Map<Object, Object> response = new HashMap<>();
        response.put("code", "SERVICE_UNAVAILABLE");
        response.put("message", "서비스 일시 중지, 잠시 후 다시 시도");
        response.put("circuitBreakerName", e.getCausingCircuitBreakerName());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}