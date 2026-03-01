package com.msa.order.domain.settlement.batch;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.entity.OrderItem;
import com.msa.order.domain.order.repository.OrderRepository;
import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementItem;
import com.msa.order.domain.settlement.entity.SettlementStatus;
import com.msa.order.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SettlementItemProcessor implements ItemProcessor<Long, SettlementData> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("#{jobParameters['date']}")
    private String dateParam;

    @Value("${settlement.commission.rate:10.0}")
    private BigDecimal commissionRate;

    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;

    @Override
    public SettlementData process(Long sellerId) {
        LocalDate settlementDate = LocalDate.parse(dateParam);

        if (settlementRepository.existsBySellerIdAndSettlementDate(sellerId, settlementDate)) {
            log.info("이미 정산됨. sellerId={}, date={}", sellerId, settlementDate);
            return null;
        }

        String dateStr = settlementDate.format(DATE_FORMATTER);
        List<OrderItem> items = orderRepository.findItemsByStatusAndDateAndSellerId(
                Order.OrderStatus.CONFIRMED, dateStr, sellerId);

        if (items.isEmpty()) return null;

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

        List<SettlementItem> settlementItems = items.stream()
                .map(item -> {
                    int salesAmount = item.getUnitPrice() * item.getQuantity();
                    int itemCommission = commissionRate
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(salesAmount))
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                    return SettlementItem.builder()
                            .settlement(settlement)
                            .orderId(item.getOrder().getOrderId())
                            .productId(item.getProductId())
                            .sellerId(sellerId)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .salesAmount(salesAmount)
                            .commissionAmount(itemCommission)
                            .netAmount(salesAmount - itemCommission)
                            .settlementDate(settlementDate)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("[정산 Batch] Processor: sellerId={}, totalSales={}, commission={}, net={}, orderCount={}",
                sellerId, totalSalesAmount, commissionAmount, netAmount, orderCount);

        return new SettlementData(settlement, settlementItems);
    }
}
