package com.example.payment.global.toss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealTossPaymentGateway implements TossPaymentGateway {

    private final TossPaymentClient tossPaymentClient;
    private final TossPaymentProperties tossPaymentProperties;

    @Override
    public TossPaymentResponse confirm(String paymentKey, String orderId, int amount) {
        return tossPaymentClient.confirm(
                tossPaymentProperties.getBasicAuthHeader(),
                new TossConfirmRequest(paymentKey, orderId, amount)
        );
    }
}
