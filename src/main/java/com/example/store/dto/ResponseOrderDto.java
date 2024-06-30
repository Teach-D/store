package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseOrderDto {

    private List<ResponseProductDto> products = new ArrayList<>();
    private String date;
    private Long id;

}
