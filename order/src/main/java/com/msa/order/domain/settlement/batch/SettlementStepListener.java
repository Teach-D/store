package com.msa.order.domain.settlement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[정산 Batch] Step 시작: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[정산 Batch] Step 종료");

        if (!stepExecution.getFailureExceptions().isEmpty()) {
            stepExecution.getFailureExceptions()
                    .forEach(e -> log.error("[정산 Batch] Step 실패 원인: ", e));
        }

        return stepExecution.getExitStatus();
    }
}
