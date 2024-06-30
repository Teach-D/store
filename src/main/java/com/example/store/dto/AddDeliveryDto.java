package com.example.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDeliveryDto {
    private String recipient;

    private String address;

    private String phoneNumber;

    private String request;


}
