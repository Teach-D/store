package com.msa.member.domain.coupon.repository;

import com.msa.member.domain.coupon.entity.Coupon;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    boolean existsByMemberAndCoupon(Member member, Coupon coupon);

    void deleteByCouponIdAndMemberId(Long couponId, Long userId);
}