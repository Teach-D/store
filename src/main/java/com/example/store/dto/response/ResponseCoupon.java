package com.example.store.dto.response;

import com.example.store.entity.Coupon;
import com.example.store.entity.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCoupon {

    private Long id;
    private String title;
    private CouponType type;
    private Integer totalQuantity;
    private int issuedQuantity;
    private int discountAmount;
    private int minAvailableAmount;
    private LocalDateTime dateIssueStart;
    private LocalDateTime dateIssueEnd;

    public static ResponseCoupon of(Coupon coupon) {
        return ResponseCoupon.builder()
                .id(coupon.getId())
                .title(coupon.getTitle())
                .type(coupon.getType())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .discountAmount(coupon.getDiscountAmount())
                .minAvailableAmount(coupon.getMinAvailableAmount())
                .dateIssueStart(coupon.getDateIssueStart())
                .dateIssueEnd(coupon.getDateIssueEnd())
                .build();
    }
}
