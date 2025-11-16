package com.msa.member.domain.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private T result;
    private boolean success;
    private String message;

    public static <T> ResponseDto<T> success(T result) {
        return new ResponseDto<>(result,true, null);
    }

    public static <T> ResponseDto<T> error(T result, String errorMessage) {
        return new ResponseDto<>(result, false, errorMessage);
    }

}
