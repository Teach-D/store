package com.msa.order.domain.fail.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.common.client.CartServiceClient;
import com.msa.order.domain.fail.entity.FailedTask;
import com.msa.order.domain.fail.repository.FailedTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedTaskScheduler {

    private final FailedTaskRepository failedTaskRepository;
    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;

//    @Scheduled(fixedDelay = 6000)
    @Transactional
    public void retryFailedTasks(){
        log.info("재시도 스케줄링 시작");

        List<FailedTask> tasks = failedTaskRepository.findByStatusAndRetryCountLessThan(
                FailedTask.TaskStatus.PENDING, 5
        );

        if (tasks.isEmpty()){
            return;
        }

        for (FailedTask task : tasks) {
            try {
                task.markAsProcessing();

                if ("CLEAR_CART".equals(task.getTaskType())){
                    retryCartClear(task);
                }

                task.markAsSuccess();

                log.info("작업 재시도 성공");
            } catch (Exception e) {
                log.info("작업 재시도 실패");

                task.incrementRetryCount();

                if (task.getRetryCount() >= 5) {
                    task.markAsFailed();
                    log.info("실패");
                } else {
                    task.markAsPending();
                }
            }

        }
    }

    private void retryCartClear(FailedTask task) throws JsonProcessingException {
        List<Long> cartItemIds = objectMapper.readValue(
                task.getTaskData(),
                new TypeReference<List<Long>>() {}
        );
        for (Long cartItemId : cartItemIds) {
            cartServiceClient.clearCartItem(cartItemId);
        }

        log.info("카드 삭제 완료");
    }


}
