package com.msa.order.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "settlement_item")
public class SettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    private Long orderId;

    private Long productId;

    private Long sellerId;

    private int quantity;

    private int unitPrice;

    private int salesAmount;

    private int commissionAmount;

    private int netAmount;

    private LocalDate settlementDate;
}
