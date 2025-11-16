package com.msa.member.domain.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.msa.member.domain.member.entity.Member;
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
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "member_id")
    private Member member;

    private String date;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CartItem> cartItemList = new ArrayList<>();
}
