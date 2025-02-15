package com.example.store.controller;

import com.example.store.dto.request.RequestCoupon;
import com.example.store.dto.response.ResponseCoupon;
import com.example.store.entity.Coupon;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.CouponIssueService;
import com.example.store.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/coupons")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;
    private final CouponIssueService couponIssueService;

    @GetMapping
    public ResponseEntity<List<ResponseCoupon>> getAllCoupons() {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseCoupon> getCoupon(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.getCoupon(id));
    }

    @PostMapping // coupon 새성
    public ResponseEntity<ResponseCoupon> saveCoupon(@Valid @RequestBody RequestCoupon requestCoupon, BindingResult bindingResult) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.saveCoupon(requestCoupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseCoupon> updateCoupon(@PathVariable Long id, @Valid @RequestBody RequestCoupon requestCoupon) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(couponService.updateCoupon(id, requestCoupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.deleteCoupon(id));
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long id, @IfLogin LoginUserDto loginUserDto) {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.issue(id, loginUserDto));
    }

    @PostMapping("/load-test/{couponId}/issue/{memberId}")
    public ResponseEntity<Void> issueCouponLoadTest(@PathVariable Long couponId, @PathVariable Long memberId) {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.issueLoadTest(couponId, memberId));
    }

    @PostMapping("/load-test/{couponId}/issue/{memberId}/redis")
    public ResponseEntity issueCouponLoadTestRedis(@PathVariable Long couponId, @PathVariable Long memberId) {
        couponIssueService.issue(couponId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
