package com.msa.order.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "settlement",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_seller_date", columnNames = {"seller_id", "settlement_date"}
    )
)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    private LocalDate settlementDate;

    private int totalSalesAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate;

    private int commissionAmount;

    private int netAmount;

    private int orderCount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
