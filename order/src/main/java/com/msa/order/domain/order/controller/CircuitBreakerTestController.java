package com.msa.order.domain.order.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/circuit")
@RequiredArgsConstructor
public class CircuitBreakerTestController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Circuit을 강제로 CLOSED 상태로 전환
     */
    @PostMapping("/close")
    public ResponseEntity<Map<String, String>> close(@RequestParam
                                                     String name) {
        circuitBreakerRegistry.circuitBreaker(name)
                .transitionToClosedState();

        Map<String, String> response = new HashMap<>();
        response.put("name", name);
        response.put("status", "CLOSED");
        response.put("message", "Circuit을 CLOSED 상태로 전환했습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * Circuit을 강제로 OPEN 상태로 전환
     */
    @PostMapping("/open")
    public ResponseEntity<Map<String, String>> open(@RequestParam
                                                    String name) {
        circuitBreakerRegistry.circuitBreaker(name)
                .transitionToOpenState();

        Map<String, String> response = new HashMap<>();
        response.put("name", name);
        response.put("status", "OPEN");
        response.put("message", "Circuit을 OPEN 상태로 전환했습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * Circuit 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam
                                                      String name) {
        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker(name);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("state", circuitBreaker.getState().toString());
        response.put("failureRate", metrics.getFailureRate() + "%");
        response.put("slowCallRate", metrics.getSlowCallRate() + "%");
        response.put("bufferedCalls",
                metrics.getNumberOfBufferedCalls());
        response.put("failedCalls", metrics.getNumberOfFailedCalls());
        response.put("successfulCalls",
                metrics.getNumberOfSuccessfulCalls());
        response.put("notPermittedCalls",
                metrics.getNumberOfNotPermittedCalls());

        return ResponseEntity.ok(response);
    }

}
