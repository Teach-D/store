package com.msa.order.domain.settlement.dto;

import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class SettlementResponse {

    private Long id;
    private Long sellerId;
    private LocalDate settlementDate;
    private int totalSalesAmount;
    private BigDecimal commissionRate;
    private int commissionAmount;
    private int netAmount;
    private int orderCount;
    private SettlementStatus status;

    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .sellerId(settlement.getSellerId())
                .settlementDate(settlement.getSettlementDate())
                .totalSalesAmount(settlement.getTotalSalesAmount())
                .commissionRate(settlement.getCommissionRate())
                .commissionAmount(settlement.getCommissionAmount())
                .netAmount(settlement.getNetAmount())
                .orderCount(settlement.getOrderCount())
                .status(settlement.getStatus())
                .build();
    }
}
