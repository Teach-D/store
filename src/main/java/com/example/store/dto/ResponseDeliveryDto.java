package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDeliveryDto {

    private String recipient;
    private String address;
    private String phoneNumber;
    private String request;
}
