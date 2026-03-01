package com.msa.order.domain.settlement.batch;

import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementItem;

import java.util.List;

public record SettlementData(
        Settlement settlement,
        List<SettlementItem> items
) {}
