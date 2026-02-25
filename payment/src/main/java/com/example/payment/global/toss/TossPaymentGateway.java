package com.example.payment.global.toss;

public interface TossPaymentGateway {
    TossPaymentResponse confirm(String paymentKey, String orderId, int amount);
}
