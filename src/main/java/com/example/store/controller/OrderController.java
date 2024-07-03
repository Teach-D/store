package com.example.store.controller;

import com.example.store.dto.AddCartItemDto;
import com.example.store.dto.ResponseOrderDto;
import com.example.store.dto.ResponseProductDto;
import com.example.store.entity.*;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/{discountId}")
    public void addOrderDiscount(@IfLogin LoginUserDto loginUserDto, @PathVariable Long discountId) {
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

        Discount discount = discountService.getDiscount(discountId);

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

        if(member.getDiscounts().contains(discount)) {
            totalPrice = totalPrice - discount.getDiscountPrice();
        }


        order.updateTotalPrice(totalPrice);

        orderService.save(order);
    }

    @PostMapping("/")
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
    }

    @GetMapping
    public List<ResponseOrderDto> getOrders(@IfLogin LoginUserDto loginUserDto) {
        log.info("aa");
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<Order> orders = orderService.findByMember(member);
        List<ResponseOrderDto> responseOrderDtos = new ArrayList<>();

        for (Order order : orders) {
            ResponseOrderDto responseOrderDto = ResponseOrderDto.builder()
                            .date(order.getDate())
                            .id(order.getOrderId())
                            .totalPrice(order.getTotalPrice())
                            .build();

            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                ResponseProductDto responseProductDto = ResponseProductDto.builder()
                                .product(product)
                                .quantity(orderItem.getQuantity())
                                .build();

                responseOrderDto.getProducts().add(responseProductDto);

            }

            responseOrderDtos.add(responseOrderDto);
        }

        return responseOrderDtos;
    }

    @GetMapping("/{orderId}")
    public ResponseOrderDto getOrder(@IfLogin LoginUserDto loginUserDto, @PathVariable Long orderId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Order order = orderService.findById(orderId);

        order.updateMember(member);
        ResponseOrderDto responseOrderDto = ResponseOrderDto
                    .builder()
                    .date(order.getDate())
                    .totalPrice(order.getTotalPrice())
                    .build();

        log.info(order.getMember().getEmail());
        if (order.getMember() == member) {

            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                ResponseProductDto responseProductDto = ResponseProductDto.builder()
                                .product(product)
                                .quantity(orderItem.getQuantity())
                                .build();
                responseOrderDto.getProducts().add(responseProductDto);
            }

            return responseOrderDto;
        } else {
            return null;
        }
    }

    @DeleteMapping("/{orderId}")
    public void cancelOrder(@IfLogin LoginUserDto loginUserDto, @PathVariable Long orderId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Order order = orderService.findById(orderId);
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.updateQuantity(product.getQuantity() + orderItem.getQuantity());
            orderItemService.delete(orderItem.getOrderId());
        }

        orderService.delete(order);
    }
}
