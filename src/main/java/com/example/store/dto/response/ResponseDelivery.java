package com.example.store.dto.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDelivery {

    private String recipient;
    private String address;
    private String phoneNumber;
    private String request;
}
