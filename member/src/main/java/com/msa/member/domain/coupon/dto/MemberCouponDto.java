package com.msa.member.domain.coupon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCouponDto {

    private Long memberId;

    private Long couponId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // ✅ JSON 직렬화 포맷 지정
    private LocalDateTime dateIssued;


}
