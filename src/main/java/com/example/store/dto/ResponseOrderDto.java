package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseOrderDto {

    private List<ResponseProductDto> products = new ArrayList<>();
    private String date;
    private Long id;
    private int totalPrice;

}
