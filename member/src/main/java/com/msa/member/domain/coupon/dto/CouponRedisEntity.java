package com.msa.member.domain.coupon.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.msa.member.domain.coupon.entity.Coupon;
import com.msa.member.domain.coupon.entity.CouponType;
import com.msa.member.global.exception.CustomException;

import java.time.LocalDateTime;

import static com.msa.member.global.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.msa.member.global.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;


public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,
        boolean availableIssueQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd
) {

    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getType(),
                coupon.getTotalQuantity(),
                coupon.availableIssueQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dateIssueStart) && now.isBefore(dateIssueEnd);
    }

    public void checkIssuableCoupon() {
        if (!availableIssueQuantity()) {
            throw new CustomException(INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!availableIssueDate()) {
            throw new CustomException(INVALID_COUPON_ISSUE_DATE);
        }
    }

}
