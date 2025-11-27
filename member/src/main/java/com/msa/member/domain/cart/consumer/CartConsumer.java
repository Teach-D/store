package com.msa.member.domain.cart.consumer;

import com.msa.member.domain.cart.dto.OrderCreatedEvent;
import com.msa.member.domain.cart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartConsumer {

    private final CartItemService cartItemService;

    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("consumer : orderId : {}", event.getOrderId());

        try {
            for (Long cartItemId : event.getCartItemIds()) {
                cartItemService.deleteCartItem(cartItemId);
                log.info("카트 아이템 삭제 cartItemId : {}", cartItemId);
            }
        } catch (Exception e) {
            log.error("카트 삭제 실패 : {}", e.getMessage());
            throw e;
        }
    }
}
