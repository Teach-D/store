
package com.msa.member.domain.coupon.repository;

import com.msa.member.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByTitle(String title);
}