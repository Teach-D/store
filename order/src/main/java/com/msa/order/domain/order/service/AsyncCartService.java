package com.msa.order.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.common.client.CartServiceClient;
import com.msa.order.domain.fail.entity.FailedTask;
import com.msa.order.domain.fail.repository.FailedTaskRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AsyncCartService {

    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;
    private final FailedTaskRepository failedTaskRepository;

    @Async
    @Retryable(
            retryFor = {FeignException.class, TimeoutException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void clearCartItems(List<Long> cartItemIds) {
        log.info("카트 삭제 시도 시작: {}", cartItemIds);

        try {
            for (Long cartItemId : cartItemIds) {
                log.info("카트 아이템 {} 삭제 요청 중...", cartItemId);
                cartServiceClient.clearCartItem(cartItemId);
                log.info(" 카트 아이템 {} 삭제 성공", cartItemId);
            }
            log.info("카트 삭제 완료: {}", cartItemIds);
        } catch (Exception e) {
            log.error("카트 삭제 실패: {}, 에러: {}", cartItemIds, e.getMessage());
            throw e;  // 재시도를 위해 예외를 다시 던짐
        }
    }

    @Recover
    public void recoverClearCartItems(Exception e, List<Long> cartItemIds) {
        log.error("카트 삭제 3회 모두 실패! DB에 기록합니다: {}", cartItemIds, e);

        try {
            String taskData = objectMapper.writeValueAsString(cartItemIds);

            FailedTask failedTask = new FailedTask(
                    "CLEAR_CART",
                    taskData,
                    e.getMessage()
            );

            failedTaskRepository.save(failedTask);
            log.info("실패 작업 DB 저장 완료: ID={}", failedTask.getId());

        } catch (JsonProcessingException ex) {
            log.error("실패 작업 저장 중 오류 발생", ex);
            throw new RuntimeException(ex);
        }
    }
}
