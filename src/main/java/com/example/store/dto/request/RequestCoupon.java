package com.example.store.dto.request;

import com.example.store.entity.Coupon;
import com.example.store.entity.CouponType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCoupon {

    @NotBlank(message = "쿠폰명을 적어주세요")
    private String title;

    @NotBlank(message = "쿠폰타입을 적어주세요 >> FIRST_COME_FIRST_SERVED")
    private String type;

    private int totalQuantity;

    @NotNull(message = "쿠폰 할인금액을 적어주세요")
    private int discountAmount;

    @NotNull(message = "쿠폰 적용을 위한 최소금액을 적어주세요")
    private int minAvailableAmount;

    @NotNull(message = "쿠폰시작 날짜를 적어주세요")
    private LocalDateTime dateIssueStart;

    @NotNull(message = "쿠폰종료 날짜를 적어주세요")
    private LocalDateTime dateIssueEnd;

    public static Coupon dtoToEntity(RequestCoupon requestCoupon) {
        return Coupon.builder()
                .title(requestCoupon.getTitle())
                .type(CouponType.parsing(requestCoupon.getType()))
                .totalQuantity(requestCoupon.getTotalQuantity())
                .issuedQuantity(0)
                .discountAmount(requestCoupon.getDiscountAmount())
                .minAvailableAmount(requestCoupon.getMinAvailableAmount())
                .dateIssueStart(requestCoupon.getDateIssueStart())
                .dateIssueEnd(requestCoupon.getDateIssueEnd())
                .build();
    }
}
