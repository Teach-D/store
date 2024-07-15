package com.example.store.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MemberDiscount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberDiscountId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    public MemberDiscount(Member member, Discount discount) {
        this.member = member;
        this.discount = discount;
    }
}
