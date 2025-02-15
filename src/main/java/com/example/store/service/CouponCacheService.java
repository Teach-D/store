package com.example.store.service;

import com.example.store.dto.CouponRedisEntity;
import com.example.store.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponService couponService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }
}
