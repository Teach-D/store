package com.example.store.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSignUp {

    private Long memberId;
    private String email;
    private String name;
    private LocalDateTime regDate;
}
