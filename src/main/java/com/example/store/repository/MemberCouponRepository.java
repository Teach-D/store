package com.example.store.repository;

import com.example.store.entity.Coupon;
import com.example.store.entity.Member;
import com.example.store.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    boolean existsByMemberAndCoupon(Member member, Coupon coupon);
}