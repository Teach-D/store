package com.example.store.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseOrder {

    @Builder.Default
    private List<ResponseProduct> products = new ArrayList<>();
    private String date;
    private Long id;
    private int totalPrice;

}
