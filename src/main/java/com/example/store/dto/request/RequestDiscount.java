package com.example.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDiscount {

    @NotBlank(message = "할인명을 적어주세요")
    private String discountName;

    @NotNull(message = "몇원을 할인할 것인지 적어주세요")
    private int discountPrice;

    @NotBlank(message = "쿠폰만료기간을 적어주세요")
    private String expirationDate;

    @NotNull(message = "할인의 수량을 적어주세요")
    private int quantity;

    private int discountCondition;
}
