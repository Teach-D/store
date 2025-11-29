package com.msa.order.domain.order.controller;

import com.msa.order.domain.order.dto.response.ResponseOrder;
import com.msa.order.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<ResponseOrder>> getOrders(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(OK).body(orderService.getOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ResponseOrder> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.status(OK).body(orderService.getOrder(orderId));
    }

    @PostMapping("/{discountId}")
    public ResponseEntity<Void> addOrderByDiscount(@RequestHeader("X-User-Id") Long userId, @PathVariable Long discountId) {
        orderService.addOrderByDiscount(userId, discountId);
        return ResponseEntity.status(OK).build();
    }

    @PostMapping
    public ResponseEntity<Void> addOrderByNoDiscount(@RequestHeader("X-User-Id") Long userId) {
        orderService.addOrderByNoDiscount(userId);
        return ResponseEntity.status(OK).build();
    }

    @PostMapping("/id/{userId}")
    public ResponseEntity<Void> addOrderByNoDiscountById(@PathVariable Long userId) {
        orderService.addOrderByNoDiscount(userId);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.status(NO_CONTENT).build();
    }


}
