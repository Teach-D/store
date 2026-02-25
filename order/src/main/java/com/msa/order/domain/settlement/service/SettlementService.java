package com.msa.order.domain.settlement.service;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.entity.OrderItem;
import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementItem;
import com.msa.order.domain.settlement.entity.SettlementStatus;
import com.msa.order.domain.settlement.repository.SettlementItemRepository;
import com.msa.order.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    @Value("${settlement.commission.rate:10.0}")
    private BigDecimal commissionRate;

    public void createDailySettlement(List<Order> orders, LocalDate settlementDate) {
        Map<Long, List<OrderItem>> itemsBySeller = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getSellerId() != null)
                .collect(Collectors.groupingBy(OrderItem::getSellerId));

        for (Map.Entry<Long, List<OrderItem>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            if (settlementRepository.existsBySellerIdAndSettlementDate(sellerId, settlementDate)) {
                log.info("[정산] 이미 집계됨: sellerId={}, date={}", sellerId, settlementDate);
                continue;
            }

            int totalSalesAmount = items.stream()
                    .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                    .sum();

            int commissionAmount = commissionRate
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(totalSalesAmount))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            int netAmount = totalSalesAmount - commissionAmount;

            long orderCount = items.stream()
                    .map(item -> item.getOrder().getOrderId())
                    .distinct()
                    .count();

            Settlement settlement = Settlement.builder()
                    .sellerId(sellerId)
                    .settlementDate(settlementDate)
                    .totalSalesAmount(totalSalesAmount)
                    .commissionRate(commissionRate)
                    .commissionAmount(commissionAmount)
                    .netAmount(netAmount)
                    .orderCount((int) orderCount)
                    .status(SettlementStatus.COMPLETED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            settlementRepository.save(settlement);

            for (OrderItem item : items) {
                int itemSalesAmount = item.getUnitPrice() * item.getQuantity();
                int itemCommission = commissionRate
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(itemSalesAmount))
                        .setScale(0, RoundingMode.HALF_UP)
                        .intValue();

                SettlementItem settlementItem = SettlementItem.builder()
                        .settlement(settlement)
                        .orderId(item.getOrder().getOrderId())
                        .productId(item.getProductId())
                        .sellerId(sellerId)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .salesAmount(itemSalesAmount)
                        .commissionAmount(itemCommission)
                        .netAmount(itemSalesAmount - itemCommission)
                        .settlementDate(settlementDate)
                        .build();

                settlementItemRepository.save(settlementItem);
            }

            log.info("[정산] sellerId={}, totalSales={}, commission={}, net={}, orderCount={}",
                    sellerId, totalSalesAmount, commissionAmount, netAmount, orderCount);
        }
    }
}
