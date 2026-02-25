package com.msa.order.domain.settlement.scheduler;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.repository.OrderRepository;
import com.msa.order.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 테스트용 import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Component
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementScheduler {

    private final OrderRepository orderRepository;
    private final SettlementService settlementService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void aggregateDailySettlement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DATE_FORMATTER);

        List<Order> confirmedOrders = orderRepository.findByStatusAndDateWithItems(
                Order.OrderStatus.CONFIRMED, dateStr
        );

        if (confirmedOrders.isEmpty()) {
            return;
        }

        settlementService.createDailySettlement(confirmedOrders, yesterday);
        log.info("[정산] 완료: date={}, orders={}", yesterday, confirmedOrders.size());
    }

    // 테스트용 수동 실행 엔드포인트
    @GetMapping("/test/run")
    @Transactional
    public String runManually(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null)
                ? LocalDate.parse(date)
                : LocalDate.now().minusDays(1);
        String dateStr = targetDate.format(DATE_FORMATTER);

        List<Order> confirmedOrders = orderRepository.findByStatusAndDateWithItems(
                Order.OrderStatus.CONFIRMED, dateStr
        );

        if (confirmedOrders.isEmpty()) {
            return "[정산 테스트] 대상 주문 없음: date=" + dateStr;
        }

        settlementService.createDailySettlement(confirmedOrders, targetDate);
        return "[정산 테스트] 완료: date=" + dateStr + ", orders=" + confirmedOrders.size();
    }
}
