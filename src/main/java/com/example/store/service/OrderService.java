package com.example.store.service;

import com.example.store.dto.ResponseDto;
import com.example.store.dto.ResponseOrderDto;
import com.example.store.dto.ResponseProductDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.*;
import com.example.store.exception.ex.DiscountException.NotFoundDiscountException;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.NotFoundOrderException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;
    private final DiscountRepository discountRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findByMember(Member member) {
        return orderRepository.findAllByMember_MemberId(member.getMemberId());
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public void delete(Order order) {
        orderRepository.delete(order);
    }

    public ResponseDto<List<ResponseOrderDto>> getOrders(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        List<Order> orders = orderRepository.findAllByMember_MemberId(member.getMemberId());
        List<ResponseOrderDto> responseOrderDtos = new ArrayList<>();

        orders.forEach(order -> {
            ResponseOrderDto responseOrderDto = ResponseOrderDto.builder()
                    .date(order.getDate())
                    .id(order.getOrderId())
                    .totalPrice(order.getTotalPrice())
                    .build();

            order.getOrderItems().forEach(orderItem -> {
                Product product = new Product(orderItem.getProduct());

                ResponseProductDto responseProductDto = ResponseProductDto.builder()
                        .product(product)
                        .quantity(orderItem.getQuantity())
                        .productTitle(orderItem.getProductTitle())
                        .productPrice(orderItem.getProductPrice())
                        .build();

                responseOrderDto.getProducts().add(responseProductDto);
            });

            responseOrderDtos.add(responseOrderDto);
        });

        return ResponseDto.success(responseOrderDtos);
    }

    public ResponseDto<ResponseOrderDto> getOrders(LoginUserDto loginUserDto, Long orderId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Order order = orderRepository.findById(orderId).orElseThrow(NotFoundOrderException::new);

        order.updateMember(member);
        ResponseOrderDto responseOrderDto = ResponseOrderDto
                .builder()
                .date(order.getDate())
                .totalPrice(order.getTotalPrice())
                .build();

        if (order.getMember() == member) {

            order.getOrderItems().forEach(orderItem -> {
                Product product = orderItem.getProduct();
                ResponseProductDto responseProductDto = ResponseProductDto.builder()
                        .product(product)
                        .quantity(orderItem.getQuantity())
                        .build();
                responseOrderDto.getProducts().add(responseProductDto);
            });

            return ResponseDto.success(responseOrderDto);
        } else {
            return null;
        }
    }

    public ResponseEntity<SuccessDto> addOrderByDiscount(LoginUserDto loginUserDto, Long discountId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        Delivery delivery = member.getDelivery();

        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate.getYear()) + (localDate.getMonthValue() < 10 ? "0" :"") + String.valueOf(localDate.getMonthValue()) + (localDate.getDayOfMonth() < 10 ? "0" :"") +String.valueOf(localDate.getDayOfMonth());

        Order order = Order.builder()
                .member(member)
                .delivery(delivery)
                .date(date)
                .build();

        Discount discount = discountRepository.findById(discountId).orElseThrow(NotFoundDiscountException::new);
        int totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .order(order)
                    .quantity(cartItem.getQuantity())
                    .build();

            order.getOrderItems().add(orderItem);

            cartItemRepository.delete(cartItem);
            orderItemRepository.save(orderItem);
            totalPrice = (totalPrice + cartItem.getProduct().getPrice() * cartItem.getQuantity());
        }

        totalPrice = totalPrice - discount.getDiscountPrice();
        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> addOrderByNoDiscount(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
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

            cartItemRepository.delete(cartItem);
            orderItemRepository.save(orderItem);
            totalPrice = (totalPrice + cartItem.getProduct().getPrice() * cartItem.getQuantity());
        }

        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteOrder(LoginUserDto loginUserDto, Long orderId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Order order = orderRepository.findById(orderId).orElseThrow(NotFoundOrderException::new);

        order.getOrderItems().forEach(orderItem -> {
            if (orderItem.getProduct() != null) {
                Product product = orderItem.getProduct();
                product.updateQuantity(product.getQuantity() + orderItem.getQuantity());

            }
        });

        orderRepository.delete(order);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }


}
