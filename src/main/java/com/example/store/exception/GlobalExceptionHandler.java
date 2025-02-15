package com.example.store.exception;

import com.example.store.exception.ex.couponException.CouponException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.example.store.exception.ex.ErrorCode.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponException.class)
    public ResponseEntity<String> handleCouponException(CouponException ex) {
        if (ex.getErrorCode() == DUPLICATE_COUPON) {
            return ResponseEntity.status(HttpStatus.OK).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        } else if (ex.getErrorCode() == NOT_FOUND_COUPON) {
            return ResponseEntity.status(HttpStatus.OK).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        } else if (ex.getErrorCode() == INVALID_COUPON_ISSUE_QUANTITY) {
            return ResponseEntity.status(HttpStatus.OK).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        } else if (ex.getErrorCode() == INVALID_COUPON_ISSUE_DATE) {
            return ResponseEntity.status(HttpStatus.OK).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        } else if (ex.getErrorCode() == DUPLICATED_COUPON_ISSUE) {
            return ResponseEntity.status(HttpStatus.OK).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(ex.getMessage());
    }

    @ExceptionHandler(MemberException1.class)
    public ResponseEntity<String> handleMemberException(CouponException ex) {
        if (ex.getErrorCode() == DUPLICATE_COUPON) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getErrorCode().getCode() + ":" + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(ex.getMessage());
    }
}