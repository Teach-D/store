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

    @PostMapping
    public void addOrder(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<CartItem> cartItems = cartItemService.getCartItems(member.getMemberId());
        Delivery delivery = member.getDelivery();
        Order order = new Order();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            order.getOrderItems().add(orderItem);
            cartItemService.deleteCartItem(member.getMemberId(), cartItem.getId());
            orderItemService.save(orderItem);
        }
        order.setMember(member);
        order.setDelivery(delivery);
        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());
        order.setDate(date);
        orderService.save(order);
    }

    @GetMapping
    public List<ResponseOrderDto> getOrders(@IfLogin LoginUserDto loginUserDto) {
        log.info("aa");
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<Order> orders = orderService.findByMember(member);
        List<ResponseOrderDto> responseOrderDto = new ArrayList<>();

        for (Order order : orders) {
            ResponseOrderDto responseOrderDto1 = new ResponseOrderDto();
            responseOrderDto1.setDate(order.getDate());
            log.info(String.valueOf(order));
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                ResponseProductDto responseProductDto = new ResponseProductDto();
                responseProductDto.setProduct(product);
                responseProductDto.setQuantity(orderItem.getQuantity());
                responseOrderDto1.getProducts().add(responseProductDto);

            }

            responseOrderDto.add(responseOrderDto1);
        }


        return responseOrderDto;
    }

    @GetMapping("/{orderId}")
    public ResponseOrderDto getOrder(@IfLogin LoginUserDto loginUserDto, @PathVariable Long orderId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Order order = orderService.findById(orderId);
        order.setMember(member);
        ResponseOrderDto responseOrderDto = new ResponseOrderDto();

        log.info(order.getMember().getEmail());
        if (order.getMember() == member) {

            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                ResponseProductDto responseProductDto = new ResponseProductDto();
                responseProductDto.setProduct(product);
                responseProductDto.setQuantity(orderItem.getQuantity());
                responseOrderDto.getProducts().add(responseProductDto);
            }

            responseOrderDto.setDate(order.getDate());
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
            product.setQuantity(product.getQuantity() + orderItem.getQuantity());
            orderItemService.delete(orderItem.getOrderId());
        }

        orderService.delete(order);
    }
}
