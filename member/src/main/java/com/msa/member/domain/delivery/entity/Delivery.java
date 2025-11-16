package com.msa.member.domain.delivery.entity;

import com.msa.member.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;

    private String address;

    private String phoneNumber;

    private String request;

    @Enumerated(EnumType.STRING)
    private DeliveryChecked deliveryChecked;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public void updateDeliver(String address, String recipient, String request, String phoneNumber) {
        this.address = address;
        this.recipient = recipient;
        this.request = request;
        this.phoneNumber = phoneNumber;
    }

    public void setChecked() {
        this.deliveryChecked = DeliveryChecked.CHECKED;
    }

    public void setUnChecked() {
        this.deliveryChecked = DeliveryChecked.UNCHECKED;
    }
}
