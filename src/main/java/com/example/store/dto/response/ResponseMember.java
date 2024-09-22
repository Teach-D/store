package com.example.store.dto.response;

import com.example.store.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMember {
    private String email;

    private String name;

    private LocalDateTime regDate;

    private Role role;

}
