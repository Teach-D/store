package com.example.store.exception;

import com.example.store.exception.ex.ErrorCode;
import lombok.Getter;

@Getter
public class MemberException1 extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public MemberException1(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
