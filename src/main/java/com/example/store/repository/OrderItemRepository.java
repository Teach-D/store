package com.example.store.repository;

import com.example.store.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    public List<OrderItem> findByOrderId(Long orderId);

    public List<OrderItem> findByProductId(Long productId);
}
