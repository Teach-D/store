package com.example.store.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSignupResponseDto {

    private Long memberId;
    private String email;
    private String name;
    private LocalDateTime regDate;
}
