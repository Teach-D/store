package com.msa.order.domain.settlement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;


@Slf4j
@Component
public class SettlementJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[정산 Batch] Job 시작 jobId={}, params={}",
                jobExecution.getId(), jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String date = jobExecution.getJobParameters().getString("date");
        BatchStatus status = jobExecution.getStatus();

        if (status == BatchStatus.COMPLETED) {
            long elapsed = Duration.between(
                    Objects.requireNonNull(jobExecution.getStartTime()),
                    jobExecution.getEndTime()
            ).toMillis();
            log.info("[정산 Batch] Job 완료 date={}, jobId={}, 소요시간={}ms", date, jobExecution.getId(), elapsed);

        } else if (status == BatchStatus.FAILED) {
            log.error("[정산 Batch] Job 실패 date={}, jobId={}", date, jobExecution.getId());
            jobExecution.getAllFailureExceptions()
                    .forEach(e -> log.error("[정산 Batch] 실패 원인: ", e));
        }
    }
}
