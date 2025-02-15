package com.example.store.exception.ex;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ErrorResponse {

    private String code;
    private String message;

    private ErrorResponse(final ErrorCode code) {
        this.code = code.getCode();
    }

}
