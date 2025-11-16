package com.msa.member.domain.coupon.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.member.domain.coupon.dto.CouponRedisEntity;
import com.msa.member.domain.coupon.dto.MemberCouponDto;
import com.msa.member.domain.coupon.entity.Coupon;
import com.msa.member.domain.coupon.repository.MemberCouponRepository;
import com.msa.member.domain.coupon.repository.RedisRepository;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.entity.MemberCoupon;
import com.msa.member.domain.member.service.MemberService;
import com.msa.member.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.msa.member.domain.coupon.service.CouponRedisUtils.getIssueRequestKey;
import static com.msa.member.domain.coupon.service.CouponRedisUtils.getIssueRequestQueueKey;
import static com.msa.member.global.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.msa.member.global.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;


@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueService {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;
    private final CouponIssueRedisService couponIssueRedisService;
    private final MemberService memberService;
    private final CouponService couponService;
    private final MemberCouponRepository memberCouponRepository;


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
            throw new CustomException(FAIL_COUPON_ISSUE_REQUEST);
        }
        log.info("-----------------");
    }

    private void checkInQueue(Long couponId, Long memberId) {
        Boolean sIsMember = redisRepository.sIsMember(getIssueRequestKey(couponId), String.valueOf(memberId));

        if (sIsMember) {
            throw new CustomException(DUPLICATED_COUPON_ISSUE);
        }
    }

    public void useCoupon(Long couponId, Long userId) {
        memberCouponRepository.deleteByCouponIdAndMemberId(couponId, userId);
    }
}
