package com.msa.member.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
