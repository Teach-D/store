package com.msa.member.domain.member.dto.response;

import com.msa.member.domain.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMemberDto {
    private String email;

    private String name;

    private LocalDateTime regDate;

    private Role role;

}
