package com.example.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private T result;
    private boolean success;

    public static <T> ResponseDto<T> success(T result) {
        return new ResponseDto<>(result,true);
    }

}
