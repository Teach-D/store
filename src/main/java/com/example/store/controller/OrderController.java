package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.entity.*;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.OrderRepository;
import com.example.store.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final CartItemService cartItemService;
    private final OrderService orderService;
    private final MemberService memberService;
    private final OrderItemService orderItemService;
    private final DeliveryService deliveryService;
    private final DiscountService discountService;
    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseDto<List<ResponseOrderDto>> getOrders(@IfLogin LoginUserDto loginUserDto) {
        return orderService.getOrders(loginUserDto);
    }

    @GetMapping("/{orderId}")
    public ResponseDto<ResponseOrderDto> getOrder(@IfLogin LoginUserDto loginUserDto, @PathVariable Long orderId) {
        return orderService.getOrders(loginUserDto, orderId);
    }

    @PostMapping("/{discountId}")
    public ResponseEntity<SuccessDto> addOrderByDiscount(@IfLogin LoginUserDto loginUserDto, @PathVariable Long discountId) {
        return orderService.addOrderByDiscount(loginUserDto, discountId);
    }

    @PostMapping
    public ResponseEntity<SuccessDto> addOrderByNoDiscount(@IfLogin LoginUserDto loginUserDto) {
        return orderService.addOrderByNoDiscount(loginUserDto);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<SuccessDto> deleteOrder(@IfLogin LoginUserDto loginUserDto, @PathVariable Long orderId) {
        return orderService.deleteOrder(loginUserDto, orderId);
    }
/*    @PostMapping("/")
    public void addOrder(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<CartItem> cartItems = cartItemService.getCartItems(member.getMemberId());
        Delivery delivery = member.getDelivery();

        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Order order = Order.builder()
                .member(member)
                .delivery(delivery)
                .date(date)
                .build();

        int totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .order(order)
                    .quantity(cartItem.getQuantity())
                    .build();
            order.getOrderItems().add(orderItem);
            cartItemService.deleteCartItem(member.getMemberId(), cartItem.getId());
            orderItemService.save(orderItem);
            totalPrice = (totalPrice + cartItem.getProduct().getPrice() * cartItem.getQuantity());
        }

        order.updateTotalPrice(totalPrice);

        orderService.save(order);
    }*/


}
