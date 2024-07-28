package com.example.store.exception.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Member
    DUPLICATE_EMAIL("M001", "이미 EMAIL이 존재합니다."),
    MEMBER_NOT_FOUND("M002", "아이디 또는 비밀번호가 틀렸습니다."),

    // Product
    NOT_FOUND_PRODUCT("P001", "존재하지 않는 제품입니다"),
    ALREADY_DELETE_PRODUCT("P002", "존재하지 않는 제품입니다"),

    // Delivery
    NOT_FOUNT_DELIVERY("DE001", "배송정보가 없습니다"),

    // Discount
    NOT_FOUND_DISCOUNT("DI002", "할인쿠폰이 없습니다"),

    // Cart
    NOT_FOUND_CART("C001", "해당 유저의 카트가 없습니다"),

    // CartItem
    NOT_FOUND_CART_ITEM("CI001", "카트 아이템이 없습니다."),

    // Category
    NOT_FOUND_CATEGORY("CT001", "해당 카테고리가 없습니다."),

    // Order
    NOT_FOUND_ORDER("O001", "해당 주문이 없습니다.");

    private String code;
    private String message;
}
