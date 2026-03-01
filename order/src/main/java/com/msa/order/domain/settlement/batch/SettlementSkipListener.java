package com.msa.order.domain.settlement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementSkipListener implements SkipListener<Long, SettlementData> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("[정산 Batch] Skip Read 단계 skip 발생 - 원인: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Long sellerId, Throwable t) {
        log.warn("[정산 Batch] Skip Process 단계 skip - sellerId={}, 원인: {}",
                sellerId, t.getMessage());
    }

    @Override
    public void onSkipInWrite(SettlementData data, Throwable t) {
        log.warn("[정산 Batch] Skip Write 단계 skip - sellerId={}, 원인: {}",
                data.settlement().getSellerId(), t.getMessage());
    }
}
