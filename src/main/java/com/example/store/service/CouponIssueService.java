package com.example.store.service;

import com.example.store.dto.CouponRedisEntity;
import com.example.store.dto.MemberCouponDto;
import com.example.store.entity.Coupon;
import com.example.store.entity.Member;
import com.example.store.entity.MemberCoupon;
import com.example.store.exception.ex.couponException.CouponException;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.redis.RedisRepository;
import com.example.store.utils.CouponRedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.example.store.exception.ex.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.example.store.exception.ex.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.example.store.utils.CouponRedisUtils.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueService {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;
    private final CouponIssueRedisService couponIssueRedisService;
    private final MemberService memberService;
    private final CouponService couponService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // ✅ Java 8 날짜 지원 추가
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ 날짜를 문자열로 변환

    public void issue(Long couponId, Long memberId) {
        CouponRedisEntity couponCache = couponCacheService.getCouponCache(couponId);
        couponCache.checkIssuableCoupon();
        couponIssueRedisService.checkCouponIssueQuantity(couponCache, memberId);
        issueRequest(couponId, memberId); // redis에 적재
    }

    private void issueRequest(Long couponId, Long memberId) {
        Member member = memberService.getMember(memberId);
        Coupon coupon = couponService.findCoupon(couponId);
        MemberCoupon memberCoupon = new MemberCoupon(member, coupon);

        MemberCouponDto memberCouponDto = MemberCouponDto.builder()
                .memberId(memberCoupon.getMember().getId())
                .couponId(memberCoupon.getCoupon().getId())
                .dateIssued(memberCoupon.getDateIssued())
                .build();

        log.info("-----------------");
        log.info("member:" + member.getId());
        log.info("coupon:" + coupon.getId());
        log.info("dateIssued:" + memberCoupon.getDateIssued());

        try {
            String value = objectMapper.writeValueAsString(memberCouponDto);
            log.info("value:" + value);

            checkInQueue(couponId, memberId);

            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(memberId)); // 특정 쿠폰큐에 적재
            redisRepository.rPush(getIssueRequestQueueKey(), value); // 모든 쿠폰큐에 적재
        } catch (JsonProcessingException e) {
            throw new CouponException(FAIL_COUPON_ISSUE_REQUEST, "쿠폰 요청 실패");
        }
        log.info("-----------------");
    }

    private void checkInQueue(Long couponId, Long memberId) {
        Boolean sIsMember = redisRepository.sIsMember(getIssueRequestKey(couponId), String.valueOf(memberId));

        if (sIsMember) {
            throw new CouponException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. member_id: %d, coupon_id: %d".formatted(memberId, couponId));
        }
    }
}
