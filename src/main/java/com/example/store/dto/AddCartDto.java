package com.example.store.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCartDto {
    private Long memberId;
}