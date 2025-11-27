package com.msa.order.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.common.client.CartServiceClient;
import com.msa.order.domain.fail.entity.FailedTask;
import com.msa.order.domain.fail.repository.FailedTaskRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncCartService {

    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;
    private final FailedTaskRepository failedTaskRepository;

    @Lazy
    @Autowired
    private AsyncCartService self;


    @Async
    public void clearCartItemsAsync(List<Long> cartItemIds) {
        try {
            self.clearCartItemsSync(cartItemIds);
        } catch (Exception e) {
            log.error("카트 삭제 비동기 처리 중 예외 발생", e);
        }
    }


    // 1 ~ 5번까지는 retry를 하고 그 이후에는 OPEN이기 때문에 이 메서드를 타는게 아니라 fallbackClearCartItems 메서드를 탐
    @CircuitBreaker(name = "memberService", fallbackMethod = "fallbackClearCartItems")
    @Retryable(
            retryFor = {FeignException.class, TimeoutException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void clearCartItemsSync(List<Long> cartItemIds) {
        log.info("카트 삭제 시도 시작: {}", cartItemIds);

        try {
            for (Long cartItemId : cartItemIds) {
                log.info("카트 아이템 {} 삭제 요청 중...", cartItemId);
                cartServiceClient.clearCartItem(cartItemId);
                log.info("카트 아이템 {} 삭제 성공", cartItemId);
            }
            log.info("카트 삭제 완료: {}", cartItemIds);
        } catch (Exception e) {
            log.error("카트 삭제 실패: {}, 에러: {}", cartItemIds, e.getMessage());
            throw e;  // 재시도를 위해 예외를 다시 던짐
        }
    }

    public void fallbackClearCartItems(List<Long> cartItemIds, Exception e) {

        if (e instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            log.warn("Circuit OPEN");
            log.warn("실패 내역 즉시 DB 저장: {}", cartItemIds);
        } else {
            log.warn("3회 재시도 모두 실패! DB에 저장: {}", cartItemIds);
            log.warn("실패 원인: {}", e.getMessage());
        }
        saveFailedTask(cartItemIds, e, e.getMessage());
    }


    @Recover
    public void recoverClearCartItems(Exception e, List<Long> cartItemIds) {
        log.error("Retry Recover 실행 - 3회 모두 실패");
        // 예외를 다시 던져서 Circuit Breaker가 실패를 감지하도록 함
        throw new RuntimeException(e.getMessage(), e);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTask(List<Long> cartItemIds, Exception e, String errorMessage) {
        try {
            String taskData = objectMapper.writeValueAsString(cartItemIds);

            FailedTask failedTask = new FailedTask(
                    "CLEAR_CART",
                    taskData,
                    errorMessage
            );
            failedTaskRepository.save(failedTask);
            log.info("실패 작업 DB 저장 완료: ID={}", failedTask.getId());
        } catch (JsonProcessingException ex) {
            log.error("실패 작업 저장 중 오류 발생", ex);
            throw new RuntimeException(ex);
        }
    }
}
