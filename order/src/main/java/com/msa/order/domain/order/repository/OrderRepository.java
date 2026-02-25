package com.msa.order.domain.order.repository;

import com.msa.order.domain.order.entity.Order;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByMemberId(Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :orderId")
    Optional<Order> findByOrderIdWithItems(@Param("orderId") Long orderId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems WHERE o.status = :status AND o.date = :date")
    List<Order> findByStatusAndDateWithItems(
            @Param("status") Order.OrderStatus status,
            @Param("date") String date
    );
}
