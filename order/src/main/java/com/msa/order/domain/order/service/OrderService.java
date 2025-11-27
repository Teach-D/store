package com.msa.order.domain.order.service;

import com.msa.order.common.client.*;
import com.msa.order.domain.order.dto.OrderCreatedEvent;
import com.msa.order.domain.order.dto.response.ResponseOrder;
import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.entity.OrderItem;
import com.msa.order.domain.order.repository.OrderItemRepository;
import com.msa.order.domain.order.repository.OrderRepository;
import com.msa.order.global.RabbitMQConfig;
import com.msa.order.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.msa.order.global.exception.ErrorCode.ORDER_NOT_FOUND;

@EnableAsync
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final AsyncCartService asyncCartService;
    private final RabbitTemplate rabbitTemplate;

    public List<ResponseOrder> getOrders(Long userId) {
        List<Order> orders = orderRepository.findAllByMemberId(userId);
        List<ResponseOrder> responseOrders = new ArrayList<>();

        orders.forEach(order -> {
            List<Long> productIds = new ArrayList<>();
            order.getOrderItems().forEach(orderItem -> {
                Long productId = orderItem.getProductId();
                productIds.add(productId);
            });

            ResponseOrder responseOrder = ResponseOrder.builder()
                    .date(order.getDate())
                    .id(order.getOrderId())
                    .totalPrice(order.getTotalPrice())
                    .productIds(productIds)
                    .build();

            responseOrders.add(responseOrder);
        });

        return responseOrders;
    }

    public ResponseOrder getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        List<Long> productIds = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> {
            Long productId = orderItem.getProductId();
            productIds.add(productId);
        });

        return ResponseOrder.builder()
                .date(order.getDate())
                .id(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .productIds(productIds)
                .build();
    }

    public void addOrderByDiscount(Long userId, Long discountId) {
        Long cartId = cartServiceClient.getCartId(userId);
        List<CartItemDto> cartItems = cartServiceClient.getCartItems(cartId);
        Long delivery = cartServiceClient.getDelivery(userId);
        int discountAmount = cartServiceClient.getDiscountAmount(discountId);


        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Order order = Order.builder()
                .memberId(userId)
                .deliveryId(delivery)
                .date(date)
                .build();

        int totalPrice = 0;

        for (CartItemDto cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .order(order)
                    .quantity(cartItem.getQuantity())
                    .build();

            orderItemRepository.save(orderItem);
            cartServiceClient.clearCartItem(cartItem.getCartItemId());

            totalPrice = (totalPrice + cartItem.getProductPrice() * cartItem.getQuantity());

            int productQuantity = productServiceClient.getProductQuantity(cartItem.getProductId());
            int productSaleQuantity = productServiceClient.getProductSaleQuantity(cartItem.getProductId());

            productServiceClient.updateProductQuantity(cartItem.getProductId(), productQuantity - cartItem.getQuantity());
            productServiceClient.updateProductSaleQuantity(cartItem.getProductId(), productSaleQuantity + cartItem.getQuantity());
        }

        totalPrice = totalPrice - discountAmount;
        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

        cartServiceClient.useCoupon(discountId, userId);
    }

    public void addOrderByNoDiscount(Long userId) {
        Long cartId = cartServiceClient.getCartId(userId);
        List<CartItemDto> cartItems = cartServiceClient.getCartItems(cartId);
        Long delivery = cartServiceClient.getDelivery(userId);
        List<Long> cartItemIds = new ArrayList<>();

        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Order order = Order.builder()
                .memberId(userId)
                .deliveryId(delivery)
                .date(date)
                .build();

        int totalPrice = 0;

        for (CartItemDto cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .order(order)
                    .quantity(cartItem.getQuantity())
                    .build();

            orderItemRepository.save(orderItem);
            cartItemIds.add(cartItem.getCartItemId());
//            cartServiceClient.clearCartItem(cartItem.getCartItemId());
            totalPrice = (totalPrice + cartItem.getProductPrice() * cartItem.getQuantity());

            int productQuantity = productServiceClient.getProductQuantity(cartItem.getProductId());
            int productSaleQuantity = productServiceClient.getProductSaleQuantity(cartItem.getProductId());

            productServiceClient.updateProductQuantity(cartItem.getProductId(), productQuantity - cartItem.getQuantity());
            productServiceClient.updateProductSaleQuantity(cartItem.getProductId(), productSaleQuantity + cartItem.getQuantity());

        }

        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

//        asyncCartService.clearCartItemsAsync(cartItemIds);

        OrderCreatedEvent event = OrderCreatedEvent.of(
                order.getOrderId(),
                userId,
                cartItemIds
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                event
        );

    }

    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        order.getOrderItems().forEach(orderItem -> {

            int productQuantity = productServiceClient.getProductQuantity(orderItem.getProductId());
            int productSaleQuantity = productServiceClient.getProductSaleQuantity(orderItem.getProductId());

            productServiceClient.updateProductQuantity(orderItem.getProductId(), productQuantity + orderItem.getQuantity());
            productServiceClient.updateProductSaleQuantity(orderItem.getProductId(), productSaleQuantity - orderItem.getQuantity());
        });

        orderRepository.delete(order);
    }
}
