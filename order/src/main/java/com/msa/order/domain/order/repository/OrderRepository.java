package com.msa.order.domain.order.repository;

import com.msa.order.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByMemberId(Long userId);
}
