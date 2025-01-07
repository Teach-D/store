package com.example.store.controller;

import com.example.store.dto.request.RefreshTokenDto;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.entity.Member;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String accessToken;
    private static String refreshToken;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Order(1)
    void testSignup() throws Exception {
        // Given
        RequestSignUp requestSignUp = RequestSignUp.builder()
                .email("test1234@example.com")
                .password("test1234!")
                .name("test1234")
                .build();

        // When
        mockMvc.perform(post("/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSignUp)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(2)
    void testLogin() throws Exception {
        for (Member member : memberRepository.findAll()) {
            System.out.println(member.getEmail());
        }


        // Given
        RequestSignIn requestSignIn = RequestSignIn.builder()
                .email("test1234@example.com")
                .password("test1234!")
                .build();

        // When
        ResultActions resultActions = mockMvc.perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSignIn)));

        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        accessToken = JsonPath.parse(responseContent).read("$.result.accessToken");
        refreshToken = JsonPath.parse(responseContent).read("$.result.refreshToken");

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.accessToken").value(accessToken))
                .andExpect(jsonPath("$.result.refreshToken").value(refreshToken));
    }

    @Test
    @Order(3)
    void testUserInfo() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(get("/members/info")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.email").value("test1234@example.com"))
                .andExpect(jsonPath("$.result.name").value("test1234"));
    }

    @Test
    @Order(4)
    void testRefreshToken() throws Exception {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refreshToken);

        // 기존 refreshToken 사용
        ResultActions resultActions = mockMvc.perform(post("/members/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isOk());

        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        String newAccessToken = JsonPath.parse(responseContent).read("$.result.accessToken");

        assertNotEquals(accessToken, newAccessToken, "새로운 accessToken이 성공적으로 발급되었습니다.");

        accessToken = newAccessToken;
    }

    @Test
    @Order(5)
    void testLogout() throws Exception {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refreshToken);

        mockMvc.perform(delete("/members/logout")
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isOk());

        assertTrue(refreshTokenRepository.findByTokenName(refreshToken).isEmpty());
    }

    @Test
    @Order(6)
    void testSignout() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete("/members/signout")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        String email = "test1234@example.com";

        assertTrue(memberRepository.findByEmail(email).isEmpty());
    }
}