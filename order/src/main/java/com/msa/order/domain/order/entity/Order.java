package com.msa.order.domain.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @JoinColumn(name = "member_id")
    private Long memberId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private String date;

    @JoinColumn(name = "delivery_id")
    private Long deliveryId;

    private int totalPrice;

    public void updateTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
