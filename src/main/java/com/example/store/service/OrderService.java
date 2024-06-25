package com.example.store.service;

import com.example.store.entity.Member;
import com.example.store.entity.Order;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

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

}
