package com.msa.member.domain.cart.consumer;

import com.msa.member.domain.cart.dto.OrderCreatedEvent;
import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.cart.entity.CartItem;
import com.msa.member.domain.cart.repository.CartItemRepository;
import com.msa.member.domain.cart.repository.CartRepository;
import com.msa.member.domain.cart.service.CartItemService;
import com.msa.member.domain.cart.service.CartService;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartConsumer {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    @RabbitListener(queues = "cart.delete")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("카트 아이멭 삭제 메시지 수신 : orderId : {}", event.getOrderId());

        try {
            Long userId = event.getUserId();
            Cart cart = cartRepository.findByMemberIdWithItems(userId).orElseThrow();

//            log.info("카트 아이템 목록 사이즈 {}", cart.getCartItemList().size());

            cartService.deleteCartItemsByIds(cart);
        } catch (Exception e) {
            log.error("카트 삭제 실패 : {}", e.getMessage());
            throw e;
        }
    }
}
