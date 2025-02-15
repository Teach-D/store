package com.example.store.service;

import com.example.store.dto.CouponRedisEntity;
import com.example.store.repository.redis.RedisRepository;
import com.example.store.utils.CouponRedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.store.utils.CouponRedisUtils.*;

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