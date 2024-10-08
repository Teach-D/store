package com.example.store.dto.response;

import com.example.store.entity.DeliveryChecked;
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
    private DeliveryChecked checked;
}
