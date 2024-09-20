package com.example.store.exception;

import com.example.store.exception.ex.*;
import com.example.store.exception.ex.DeliveryException.NotFoundDeliveryException;
import com.example.store.exception.ex.DiscountException.NotFoundDiscountException;
import com.example.store.exception.ex.MemberException.DuplicateEmailException;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.exception.ex.ProductException.AlreadyDeleteProductException;
import com.example.store.exception.ex.ProductException.NotFoundProductException;
import com.example.store.exception.ex.ProductException.OutOfProductException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundDeliveryException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUNT_DELIVERY, ErrorCode.NOT_FOUNT_DELIVERY.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundDiscountException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundDiscountException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUND_DISCOUNT, ErrorCode.NOT_FOUND_DISCOUNT.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.DUPLICATE_EMAIL, ErrorCode.DUPLICATE_EMAIL.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundMemberException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundMemberException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.MEMBER_NOT_FOUND, ErrorCode.MEMBER_NOT_FOUND.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyDeleteProductException.class)
    public ResponseEntity<ErrorResponse> alreadyDeleteProductException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.ALREADY_DELETE_PRODUCT, ErrorCode.ALREADY_DELETE_PRODUCT.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundProductException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundProductException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUND_PRODUCT, ErrorCode.NOT_FOUND_PRODUCT.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OutOfProductException.class)
    public ResponseEntity<ErrorResponse> handleOutOfProductException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.OUT_OF_PRODUCT, ErrorCode.OUT_OF_PRODUCT.getMessage()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundCartException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundCartException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUND_CART, ErrorCode.NOT_FOUND_CART.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundCategoryException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundCategoryException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUND_CATEGORY, ErrorCode.NOT_FOUND_CATEGORY.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundOrderException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundOrderException() {
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.NOT_FOUND_ORDER, ErrorCode.NOT_FOUND_ORDER.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
