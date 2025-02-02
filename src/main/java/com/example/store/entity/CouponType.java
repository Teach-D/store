package com.example.store.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CouponType {

    FIRST_COME_FIRST_SERVED;

    @JsonCreator
    public static CouponType parsing(String inputValue) {
        return Stream.of(CouponType.values())
                .filter(couponType -> couponType.toString().equals(inputValue.toUpperCase()))
                .findFirst()
                .orElse(null);
    }}
