package com.msa.order.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException ex) {
        log.warn("CustomException 발생: {}", ex.getErrorCode().getMessage());
        return ErrorResponseEntity.toResponseEntity(ex.getErrorCode());
    }
}