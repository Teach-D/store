package com.msa.member.domain.delivery.dto.response;

import com.msa.member.domain.delivery.entity.DeliveryChecked;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
