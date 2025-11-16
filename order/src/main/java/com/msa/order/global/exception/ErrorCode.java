package com.msa.order.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ============ Member 관련 (1000번대) ============
    MEMBER_NOT_FOUND(NOT_FOUND, 1001, "[Member] 사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(CONFLICT, 1002, "[Member] 이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(UNAUTHORIZED, 1003, "[Member] 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_MEMBER(UNAUTHORIZED, 1004, "[Member] 인증되지 않은 사용자입니다."),

    // ============ Delivery 관련 (2000번대) ============
    DELIVERY_NOT_FOUND(NOT_FOUND, 2001, "[Delivery] 배송 정보를 찾을 수 없습니다."),
    INVALID_DELIVERY_ADDRESS(BAD_REQUEST, 2002, "[Delivery] 유효하지 않은 배송 주소입니다."),

    // ============ Cart 관련 (3000번대) ============
    CART_NOT_FOUND(NOT_FOUND, 3001, "[Cart] 장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(NOT_FOUND, 3002, "[CartItem] 장바구니 물품을 찾을 수 없습니다."),
    CART_ITEM_ALREADY_EXISTS(CONFLICT, 3003, "[CartItem] 이미 장바구니에 존재하는 상품입니다."),
    INVALID_CART_QUANTITY(BAD_REQUEST, 3004, "[CartItem] 유효하지 않은 수량입니다."),

    // ============ Product 관련 (4000번대) ============
    PRODUCT_NOT_FOUND(NOT_FOUND, 4001, "[Product] 상품을 찾을 수 없습니다."),
    OUT_OF_PRODUCT_QUANTITY(BAD_REQUEST, 4002, "[Product] 상품 재고가 부족합니다."),
    PRODUCT_SERVICE_UNAVAILABLE(SERVICE_UNAVAILABLE, 4003, "[Product] 상품 서비스가 일시적으로 이용 불가합니다."),

    // ============ Coupon 관련 (5000번대) ============
    NOT_FOUND_COUPON(NOT_FOUND, 5001, "[Coupon] 쿠폰을 찾을 수 없습니다."),
    DUPLICATE_COUPON(CONFLICT, 5002, "[Coupon] 이미 발급받은 쿠폰입니다."),
    DUPLICATED_COUPON_ISSUE(CONFLICT, 5003, "[Coupon] 이미 발급 요청한 쿠폰입니다."),
    INVALID_COUPON_ISSUE_QUANTITY(BAD_REQUEST, 5004, "[Coupon] 발급 가능 수량을 초과했습니다."),
    INVALID_COUPON_ISSUE_DATE(BAD_REQUEST, 5005, "[Coupon] 쿠폰 발급 기간이 아닙니다."),
    FAIL_COUPON_ISSUE_REQUEST(INTERNAL_SERVER_ERROR, 5006, "[Coupon] 쿠폰 발급 요청에 실패했습니다."),
    COUPON_EXPIRED(BAD_REQUEST, 5007, "[Coupon] 만료된 쿠폰입니다."),
    COUPON_ALREADY_USED(BAD_REQUEST, 5008, "[Coupon] 이미 사용된 쿠폰입니다."),

    // ============ JWT/Auth 관련 (6000번대) ============
    INVALID_TOKEN(UNAUTHORIZED, 6001, "[Auth] 유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(UNAUTHORIZED, 6002, "[Auth] 만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, 6003, "[Auth] 유효하지 않은 리프레시 토큰입니다."),

    // ============ 공통 에러 (9000번대) ============
    INTERNAL_SERVER_ERROR_CODE(INTERNAL_SERVER_ERROR, 9001, "[Server] 서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(BAD_REQUEST, 9002, "[Input] 유효하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 9003, "[Request] 허용되지 않은 메서드입니다."),
    INVALID_TYPE_VALUE(BAD_REQUEST, 9004, "[Input] 유효하지 않은 타입입니다."),
    ORDER_NOT_FOUND(BAD_REQUEST, 9004, "[Input] 유효하지 않은 타입입니다."),
    ;
    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
