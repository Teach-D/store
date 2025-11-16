package com.msa.member.domain.coupon.controller;

import com.msa.member.domain.coupon.dto.request.RequestCoupon;
import com.msa.member.domain.coupon.dto.response.ResponseCoupon;
import com.msa.member.domain.coupon.service.CouponIssueService;
import com.msa.member.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping("/amount/{couponId}")
    public int getDiscountAmount(@PathVariable Long couponId) {
        return couponService.getDiscountAmount(couponId);
    }


    @PostMapping // coupon 새성
    public ResponseEntity<ResponseCoupon> saveCoupon(@RequestBody RequestCoupon requestCoupon, BindingResult bindingResult) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.saveCoupon(requestCoupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseCoupon> updateCoupon(@PathVariable Long id, @RequestBody RequestCoupon requestCoupon) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(couponService.updateCoupon(id, requestCoupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(couponService.deleteCoupon(id));
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        couponService.issue(id, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
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

    @DeleteMapping("/use/{couponId}")
    public ResponseEntity<Void> useCoupon(@PathVariable Long couponId, @RequestHeader("X-User-Id") Long userId) {
        couponIssueService.useCoupon(couponId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
