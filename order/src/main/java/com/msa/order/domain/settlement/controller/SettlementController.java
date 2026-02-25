package com.msa.order.domain.settlement.controller;

import com.msa.order.domain.settlement.dto.SettlementItemResponse;
import com.msa.order.domain.settlement.dto.SettlementResponse;
import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementItem;
import com.msa.order.domain.settlement.repository.SettlementItemRepository;
import com.msa.order.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @RequestParam Long sellerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<Settlement> settlements = settlementRepository.findBySellerIdAndSettlementDate(sellerId, date);
        List<SettlementResponse> response = settlements.stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{settlementId}/items")
    public ResponseEntity<List<SettlementItemResponse>> getSettlementItems(
            @PathVariable Long settlementId
    ) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));
        List<SettlementItem> items = settlementItemRepository.findBySettlement(settlement);
        List<SettlementItemResponse> response = items.stream()
                .map(SettlementItemResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
