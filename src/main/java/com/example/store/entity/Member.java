package com.example.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String name;

    @Column(length = 500)
    private String password;

    @CreationTimestamp
    private LocalDateTime regDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    @Builder.Default
    private List<MemberDiscount> discounts = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "memberId=" + memberId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", regDate=" + regDate +
                '}';
    }

    public void addRole(Role role) {
        this.role = role;
    }

    public void addDiscount(Discount discount) {
        MemberDiscount memberDiscount = new MemberDiscount(this, discount);
        discounts.add(memberDiscount);
    }

    public void addDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public void emptyDelivery() {
        this.delivery = null;
    }
}
