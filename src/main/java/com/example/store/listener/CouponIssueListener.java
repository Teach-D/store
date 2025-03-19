package com.example.store.listener;

import com.example.store.dto.MemberCouponDto;
import com.example.store.entity.MemberCoupon;
import com.example.store.repository.redis.RedisRepository;
import com.example.store.service.CouponService;
import com.example.store.utils.CouponRedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.example.store.utils.CouponRedisUtils.*;

@RequiredArgsConstructor
@EnableScheduling
//@Component
@Slf4j
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper;
    private final CouponService couponService;

    @Scheduled(fixedRate = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("발급 큐에서 발급 준비");
        while (existCouponIssueTarget()) {
            MemberCouponDto issueTarget = getIssueTarget();
            log.info("발급 시작 : " + issueTarget);
            couponService.issue(issueTarget.getCouponId(), issueTarget.getMemberId());
            log.info("발급 완료 : " + issueTarget);
            removeIssuedTarget();
        }
    }

    private boolean existCouponIssueTarget() {
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    private MemberCouponDto getIssueTarget() throws JsonProcessingException {
        String s = redisRepository.lIndex(issueRequestQueueKey, 0L);
        log.info("s: " + s);

        return objectMapper.readValue(s, MemberCouponDto.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }
}
