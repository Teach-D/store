package com.example.store.dto.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDelivery {
    private String recipient;

    private String address;

    private String phoneNumber;

    private String request;


}
