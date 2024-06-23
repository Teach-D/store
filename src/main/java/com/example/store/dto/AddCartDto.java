package com.example.store.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartDto {
    private Long memberId;
}