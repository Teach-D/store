package com.example.payment.global.toss;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentClient", url = "${toss.payments.base-url}")
public interface TossPaymentClient {

    @PostMapping("/v1/payments/confirm")
    TossPaymentResponse confirm(
            @RequestHeader("Authorization") String authorization,
            @RequestBody TossConfirmRequest request
    );
}
