package com.example.store.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
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

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @ManyToMany
    @JoinTable(name = "member_role",
        joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "member_discount",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    private List<Discount> discounts = new ArrayList<>();

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
        roles.add(role);
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }
}
