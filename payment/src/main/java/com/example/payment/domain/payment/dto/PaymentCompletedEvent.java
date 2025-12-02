package com.example.payment.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCompletedEvent {

    private Long orderId;
    private Long paymentId;
    private int amount;
}
