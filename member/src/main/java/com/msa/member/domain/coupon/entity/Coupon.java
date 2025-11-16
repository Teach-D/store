package com.msa.member.domain.coupon.entity;

import com.msa.member.common.BaseTimeEntity;
import com.msa.member.domain.coupon.dto.request.RequestCoupon;
import com.msa.member.domain.member.entity.MemberCoupon;
import com.msa.member.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.msa.member.global.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.msa.member.global.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType type;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    @OneToMany(mappedBy = "coupon", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private List<MemberCoupon> memberCoupons = new ArrayList<>();

    public void update(RequestCoupon requestCoupon) {
        this.title = requestCoupon.getTitle();
        this.type = CouponType.parsing(requestCoupon.getType());
        this.totalQuantity = requestCoupon.getTotalQuantity();
        this.discountAmount = requestCoupon.getDiscountAmount();
        this.minAvailableAmount = requestCoupon.getMinAvailableAmount();
        this.dateIssueStart = requestCoupon.getDateIssueStart();
        this.dateIssueEnd = requestCoupon.getDateIssueEnd();
    }

    public void addMemberCoupon(MemberCoupon memberCoupon) {
        this.memberCoupons.add(memberCoupon);
    }

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate() {
        return dateIssueStart.isBefore(LocalDateTime.now()) && dateIssueEnd.isAfter(LocalDateTime.now());
    }

    public void issue() {
        if (!availableIssueQuantity()) {
            throw new CustomException(INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!availableIssueDate()) {
            throw new CustomException(INVALID_COUPON_ISSUE_DATE);
        }
        issuedQuantity++;
    }
}
