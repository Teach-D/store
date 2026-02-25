package com.msa.order.domain.settlement.dto;

import com.msa.order.domain.settlement.entity.SettlementItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SettlementItemResponse {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long sellerId;
    private int quantity;
    private int unitPrice;
    private int salesAmount;
    private int commissionAmount;
    private int netAmount;
    private LocalDate settlementDate;

    public static SettlementItemResponse from(SettlementItem item) {
        return SettlementItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .productId(item.getProductId())
                .sellerId(item.getSellerId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .salesAmount(item.getSalesAmount())
                .commissionAmount(item.getCommissionAmount())
                .netAmount(item.getNetAmount())
                .settlementDate(item.getSettlementDate())
                .build();
    }
}
