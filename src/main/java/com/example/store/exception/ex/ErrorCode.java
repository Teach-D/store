package com.example.store.exception.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Member
    DUPLICATE_EMAIL("M001"),
    MEMBER_NOT_FOUND("M002"),

    // Product
    NOT_FOUND_PRODUCT("P001"),
    ALREADY_DELETE_PRODUCT("P002"),
    OUT_OF_PRODUCT("P003"),

    // Delivery
    NOT_FOUNT_DELIVERY("DE001"),

    // Coupon
    DUPLICATE_COUPON("CP001"),
    NOT_FOUND_COUPON("CP002"),
    INVALID_COUPON_ISSUE_QUANTITY("CP003"),
    INVALID_COUPON_ISSUE_DATE("CP004"),
    DUPLICATED_COUPON_ISSUE("CP005"),
    FAIL_COUPON_ISSUE_REQUEST("CP006"),
    // Cart
    NOT_FOUND_CART("C001"),

    // CartItem
    NOT_FOUND_CART_ITEM("CI001"),

    // Category
    NOT_FOUND_CATEGORY("CT001"),

    // Order
    NOT_FOUND_ORDER("O001");

    private String code;
}
