package com.msa.order.domain.settlement.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJobLauncher {

    private final Job dailySettlementJob;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 45 22 * * *")
    public void runDailySettlement() {
        run(LocalDate.now().minusDays(1), false);
    }

    public void run(LocalDate date, boolean forceRerun) {
        JobParameters params = new JobParametersBuilder()
                .addString("date", date.toString())
                .toJobParameters();
        try {
            JobExecution execution = jobLauncher.run(dailySettlementJob, params);
            log.info("[정산 Batch] 실행 완료: date={}, status={}, jobExecutionId={}",
                    date, execution.getStatus(), execution.getId());

        } catch (JobInstanceAlreadyCompleteException e) {
            if (forceRerun) {
                log.warn("[정산 Batch] 이미 완료된 Job을 강제 재실행: date={}", date);
                forceRun(date);
            } else {
                log.info("[정산 Batch] 이미 완료된 Job 재실행 X: date={}", date);
            }

        } catch (Exception e) {
            log.error("[정산 Batch] 오류: date={}", date, e);
        }
    }

    private void forceRun(LocalDate date) {
        JobParameters forceParams = new JobParametersBuilder()
                .addString("date", date.toString())
                .addLong("retry", System.currentTimeMillis())
                .toJobParameters();
        try {
            JobExecution execution = jobLauncher.run(dailySettlementJob, forceParams);
            log.info("[정산 Batch] 강제 재실행 완료: date={}, status={}", date, execution.getStatus());
        } catch (Exception e) {
            log.error("[정산 Batch] 강제 재실행 실패: date={}", date, e);
        }
    }
}
