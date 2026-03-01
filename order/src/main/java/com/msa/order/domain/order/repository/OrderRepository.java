package com.msa.order.domain.order.repository;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.entity.OrderItem;
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

    // Spring Batch용 쿼리: 해당 날짜에 주문이 있는 판매자 ID 목록 조회
    @Query("SELECT DISTINCT oi.sellerId FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = :status AND o.date = :date AND oi.sellerId IS NOT NULL")
    List<Long> findDistinctSellerIdsByStatusAndDate(
            @Param("status") Order.OrderStatus status,
            @Param("date") String date
    );

    // Spring Batch용 쿼리: 특정 판매자의 주문 항목만 조회
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o " +
           "WHERE o.status = :status AND o.date = :date AND oi.sellerId = :sellerId")
    List<OrderItem> findItemsByStatusAndDateAndSellerId(
            @Param("status") Order.OrderStatus status,
            @Param("date") String date,
            @Param("sellerId") Long sellerId
    );
}
