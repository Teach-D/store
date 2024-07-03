package com.example.store.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
    @NotEmpty
    String refreshToken;
}