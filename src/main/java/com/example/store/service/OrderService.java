package com.example.store.service;

import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseOrder;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.dto.response.SuccessDto;
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

    public ResponseDto<List<ResponseOrder>> getOrders(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        List<Order> orders = orderRepository.findAllByMember_MemberId(member.getMemberId());
        List<ResponseOrder> responseOrders = new ArrayList<>();

        orders.forEach(order -> {
            ResponseOrder responseOrder = ResponseOrder.builder()
                    .date(order.getDate())
                    .id(order.getOrderId())
                    .totalPrice(order.getTotalPrice())
                    .build();

            order.getOrderItems().forEach(orderItem -> {
                Product product = new Product(orderItem.getProduct());

                ResponseProduct responseProduct = ResponseProduct.builder()
                        .product(product)
                        .quantity(orderItem.getQuantity())
                        .productTitle(orderItem.getProductTitle())
                        .productPrice(orderItem.getProductPrice())
                        .build();

                responseOrder.getProducts().add(responseProduct);
            });

            responseOrders.add(responseOrder);
        });

        return ResponseDto.success(responseOrders);
    }

    public ResponseDto<ResponseOrder> getOrders(LoginUserDto loginUserDto, Long orderId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Order order = orderRepository.findById(orderId).orElseThrow(NotFoundOrderException::new);

        order.updateMember(member);
        ResponseOrder responseOrder = ResponseOrder
                .builder()
                .date(order.getDate())
                .totalPrice(order.getTotalPrice())
                .build();

        if (order.getMember() == member) {

            order.getOrderItems().forEach(orderItem -> {
                Product product = orderItem.getProduct();
                ResponseProduct responseProduct = ResponseProduct.builder()
                        .product(product)
                        .quantity(orderItem.getQuantity())
                        .build();
                responseOrder.getProducts().add(responseProduct);
            });

            return ResponseDto.success(responseOrder);
        } else {
            return null;
        }
    }

    public ResponseEntity<SuccessDto> addOrderByDiscount(LoginUserDto loginUserDto, Long discountId) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(NotFoundCartException::new);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        Delivery delivery = member.getDeliveries().stream()
                .filter(d -> d.getDeliveryChecked().equals(DeliveryChecked.CHECKED))
                .findFirst().get();

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
        Delivery delivery = member.getDeliveries().stream()
                .filter(d -> d.getDeliveryChecked().equals(DeliveryChecked.CHECKED))
                .findFirst().get();

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
                    .productTitle(cartItem.getProduct().getTitle())
                    .productPrice(cartItem.getProduct().getPrice())
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
                product.updateSaleQuantity(product.getSaleQuantity() - orderItem.getQuantity());
            }
        });

        orderRepository.delete(order);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }


}
