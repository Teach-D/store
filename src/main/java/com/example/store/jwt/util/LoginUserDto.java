package com.example.store.jwt.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {
    private String email;
    private String name;
    private Long memberId;
    private List<String> roles = new ArrayList<>();

    public void addRole(String role){
        roles.add(role);
    }

    @Builder
    public LoginUserDto(String email, String name) {
        this.email = email;
        this.name = name;
    }

}