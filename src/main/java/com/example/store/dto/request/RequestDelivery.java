package com.example.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDelivery {

    @NotBlank(message = "수령인의 이름을 적어주세요")
    private String recipient;

    @NotBlank(message = "주소를 적어주세요")
    private String address;

    private String phoneNumber;

    private String request;


}
