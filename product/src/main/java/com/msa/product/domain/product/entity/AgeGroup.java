package com.msa.product.domain.product.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public enum AgeGroup {
    AGE_10S, AGE_20S, AGE_30S, AGE_40S, AGE_50S_PLUS;

    public static AgeGroup fromBirthDate(LocalDateTime birthDate) {
        int age = Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
        if (age < 20) return AGE_10S;
        if (age < 30) return AGE_20S;
        if (age < 40) return AGE_30S;
        if (age < 50) return AGE_40S;
        return AGE_50S_PLUS;
    }
}
