package com.example.store.entity;

import jakarta.persistence.*;
import lombok.*;

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

    public void updateDeliver(String address, String recipient, String request, String phoneNumber) {
        this.address = address;
        this.recipient = recipient;
        this.request = request;
        this.phoneNumber = phoneNumber;
    }
}
