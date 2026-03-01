package com.msa.order.domain.settlement.batch;

import com.msa.order.domain.settlement.repository.SettlementItemRepository;
import com.msa.order.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<SettlementData> {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    @Override
    public void write(Chunk<? extends SettlementData> chunk) {
        for (SettlementData data : chunk) {
            settlementRepository.save(data.settlement());
            settlementItemRepository.saveAll(data.items());
        }
        log.info("[정산 Batch] Writer: {}건 저장 완료", chunk.size());
    }
}
