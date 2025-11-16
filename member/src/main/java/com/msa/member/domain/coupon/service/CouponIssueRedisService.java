package com.msa.member.domain.coupon.service;


import com.msa.member.domain.coupon.dto.CouponRedisEntity;
import com.msa.member.domain.coupon.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.msa.member.domain.coupon.service.CouponRedisUtils.getIssueRequestKey;


@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity couponRedisEntity, Long memberId) {

    }

    // 쿠폰큐의 사이즈 체크
    public boolean availableTotalIssueQuantity(Integer totalQuantity, Long couponId) {
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    // 중복되어 발급되었는지 체크
    public boolean availableMemberIssue(Long couponId, Long memberId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(memberId));
    }
}