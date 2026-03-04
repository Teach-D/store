package com.msa.product.domain.product.controller;

import com.msa.product.domain.product.dto.response.RankingResponse;
import com.msa.product.domain.product.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/daily")
    public ResponseEntity<RankingResponse> getDailyRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rankingService.getRanking("daily", limit));
    }

    @GetMapping("/weekly")
    public ResponseEntity<RankingResponse> getWeeklyRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rankingService.getRanking("weekly", limit));
    }

    @GetMapping("/monthly")
    public ResponseEntity<RankingResponse> getMonthlyRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rankingService.getRanking("monthly", limit));
    }
}
