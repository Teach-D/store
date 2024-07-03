package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    private String date;

    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private int totalPrice;

    public void updateTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void updateMember(Member member) {
        this.member = member;
    }
}
