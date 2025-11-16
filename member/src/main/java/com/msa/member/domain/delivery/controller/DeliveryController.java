package com.msa.member.domain.delivery.controller;

import com.msa.member.domain.delivery.dto.request.RequestDelivery;
import com.msa.member.domain.delivery.dto.response.ResponseDelivery;
import com.msa.member.domain.delivery.service.DeliveryService;
import com.msa.member.domain.member.dto.response.SuccessDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<ResponseDelivery>> getDeliveriesByMember(@RequestHeader("X-User-Id") Long userId) {
        List<ResponseDelivery> deliveries = deliveryService.getDeliveries(userId);
        return ResponseEntity.status(HttpStatus.OK).body(deliveries);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<ResponseDelivery> getDeliveryById(@PathVariable Long deliveryId) {
        ResponseDelivery delivery = deliveryService.getDelivery(deliveryId);
        return ResponseEntity.status(HttpStatus.OK).body(delivery);
    }

    @GetMapping("/check")
    public ResponseEntity<ResponseDelivery> getDeliveryByIdChecked(@RequestHeader("X-User-Id") Long userId) {
        ResponseDelivery deliveryByIdChecked = deliveryService.getDeliveryByIdChecked(userId);
        return ResponseEntity.status(HttpStatus.OK).body(deliveryByIdChecked);
    }

    @GetMapping("/user/{userId}")
    public Long getDeliveryId(@PathVariable Long userId) {
        return deliveryService.getDeliveryId(userId);
    }

    @PostMapping
    public ResponseEntity setDelivery(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestDelivery requestDelivery, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        deliveryService.setDelivery(userId, requestDelivery);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/check/{deliveryId}")
    public ResponseEntity updateDeliveryChecked(@RequestHeader("X-User-Id") Long userId, @PathVariable Long deliveryId) {
        deliveryService.updateDeliveryCheck(userId, deliveryId);
        return ResponseEntity.status(HttpStatus.OK).build();

    }



    @PatchMapping("/{id}")
    public ResponseEntity setDelivery(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody RequestDelivery requestDelivery, BindingResult bindingResult, @PathVariable Long deliveryId) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        deliveryService.setDelivery(userId, requestDelivery, deliveryId);
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> deleteDelivery(@RequestHeader("X-User-Id") Long userId, @PathVariable Long deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
