package com.example.store.controller;

import com.example.store.dto.request.RequestCategory;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.entity.Category;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@DirtiesContext
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private static String accessToken;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeAll
    static void beforeLogin(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {

        // signup
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

        // login
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

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.accessToken").value(accessToken));
    }

    @Test
    @Order(1)
    void addCategory() throws Exception {
        // given
        RequestCategory requestCategory1 = RequestCategory.builder()
                .name("category1")
                .build();

        // When
        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCategory1)))
                .andExpect(status().isOk())
                .andDo(print());


        // given
        RequestCategory requestCategory2 = RequestCategory.builder()
                .name("category2")
                .build();

        // When
        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCategory2)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(2)
    void getAllCategories() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(get("/categories")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].name").value("category1"))
                .andExpect(jsonPath("$.result[1].name").value("category2"));
    }

    @Test
    @Order(3)
    void getCategoryById() throws Exception {
        // given
        Category category = categoryRepository.findByName("category1").orElseThrow();
        Long categoryId = category.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/categories/" + categoryId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.name").value("category1"));
    }

    @Test
    @Order(4)
    void updateCategory() throws Exception {
        // given
        Category category = categoryRepository.findByName("category1").orElseThrow();
        Long categoryId = category.getId();

        RequestCategory requestCategory = RequestCategory.builder()
                .name("new-category1")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCategory)));

        // then
        try {
            resultActions.andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.result.name").value("new-category1"));

            Category updatedCategory = categoryRepository.findById(categoryId).orElseThrow();
            assertEquals("new-category1", updatedCategory.getName());
        } catch (AssertionError e) {
            Category currentCategory = categoryRepository.findById(categoryId).orElseThrow();
            assertEquals("category1", currentCategory.getName(), "데이터베이스의 상태가 변경되지 않아야 합니다.");

            throw e;
        }
    }

    @Test
    @Order(5)
    void deleteCategory() throws Exception {
        // given
        Category category = categoryRepository.findByName("new-category1").orElseThrow();
        Long categoryId = category.getId();

        // when
        mockMvc.perform(delete("/categories/" + categoryId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(categoryRepository.findById(categoryId).isEmpty());
    }

}