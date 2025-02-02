package com.example.store.exception.ex.couponException;

import com.example.store.exception.ex.ErrorCode;
import lombok.Getter;

@Getter
public class CouponException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public CouponException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
