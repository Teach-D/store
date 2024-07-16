package com.example.store.repository;

import com.example.store.entity.OrderItem;
import com.example.store.entity.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
    public List<OrderItem> findByOrderId(Long orderId);

    public List<OrderItem> findByProductId(Long productId);
}
