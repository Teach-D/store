package com.example.store.dto;

import com.example.store.entity.Role;
import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMemberDto {
    private String email;

    private String name;

    private LocalDateTime regDate;

    private Set<Role> roles = new HashSet<>();

}
