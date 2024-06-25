package com.example.store.dto;

import com.example.store.entity.Product;
import lombok.Data;

@Data
public class ResponseProductDto {

    private Product product;
    private int quantity;

}
