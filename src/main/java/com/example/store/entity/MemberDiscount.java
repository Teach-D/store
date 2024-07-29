package com.example.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MemberDiscount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberDiscountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "discount_id")
    private Discount discount;

    public MemberDiscount(Member member, Discount discount) {
        this.member = member;
        this.discount = discount;
    }
}
