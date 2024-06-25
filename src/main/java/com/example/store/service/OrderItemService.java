package com.example.store.service;

import com.example.store.entity.OrderItem;
import com.example.store.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public void delete(Long orderItemId) {
        orderItemRepository.deleteById(orderItemId);
    }

}
