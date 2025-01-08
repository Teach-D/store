package com.example.store.controller;

import com.example.store.dto.request.*;
import com.example.store.entity.Product;
import com.example.store.entity.Tag;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.ProductTagRepository;
import com.example.store.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTagRepository productTagRepository;

    private static String accessToken;


    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {

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

        // add category
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

        // add product
        // given
        RequestProduct requestProduct1 = RequestProduct.builder()
                .title("product1")
                .description("product1")
                .price(10000)
                .categoryId(1L)
                .imageUrl(null)
                .quantity(100)
                .build();

        // when
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestProduct1)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(1)
    void addTag() throws Exception {
        // given
        RequestTag requestTag1 = RequestTag.builder().name("tag1").build();
        RequestTag requestTag2 = RequestTag.builder().name("tag2").build();

        // when
        mockMvc.perform(post("/tags")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTag1)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post("/tags")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTag2)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(2)
    void setTag() throws Exception {
        // given 1
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        Tag tag1 = tagRepository.findByName("tag1").orElseThrow();
        Long tag1Id = tag1.getId();

        // When
        ResultActions resultActions1 = mockMvc.perform(post("/tags/" + productId + "/" + tag1Id)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions1.andExpect(status().isOk())
                .andDo(print());
        assertTrue(productTagRepository.findByTagIdAndProductId(tag1Id, productId).isPresent());

        // given 2
        Tag tag2 = tagRepository.findByName("tag2").orElseThrow();
        Long tag2Id = tag2.getId();

        // When
        ResultActions resultActions2 = mockMvc.perform(post("/tags/" + productId + "/" + tag2Id)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions2.andExpect(status().isOk())
                .andDo(print());
        assertTrue(productTagRepository.findByTagIdAndProductId(tag2Id, productId).isPresent());
    }

    @Test
    @Order(3)
    void getTag() throws Exception {
        // given
        Tag tag = tagRepository.findByName("tag1").orElseThrow();
        Long tagId = tag.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/tags/" + tagId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.name").value("tag1"));
    }

    @Test
    @Order(4)
    void getTagsByProduct() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        ResultActions resultActions = mockMvc.perform(get("/tags/products/" + productId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result[0].name").value("tag1"))
                .andExpect(jsonPath("$.result[1].name").value("tag2"));
    }

    @Test
    @Order(5)
    void updateTag() throws Exception {
        // given
        Tag tag = tagRepository.findByName("tag1").orElseThrow();
        Long tagId = tag.getId();

        RequestTag requestTag = RequestTag.builder().name("new-tag1").build();

        // when
        ResultActions resultActions = mockMvc.perform(patch("/tags/" + tagId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestTag)));

        // then
        try {
            resultActions.andExpect(status().isOk())
                    .andDo(print());

            Tag updateTag = tagRepository.findById(tagId).orElseThrow();
            assertEquals("new-tag1", updateTag.getName());
        } catch (AssertionError e) {
            Tag currentTag = tagRepository.findById(tagId).orElseThrow();
            assertEquals("tag1", currentTag.getName(), "데이터베이스의 상태가 변경되지 않아야 합니다.");

            throw e;
        }
    }

    @Test
    @Order(6)
    void deleteTag() throws Exception {
        // given
        Tag tag = tagRepository.findByName("new-tag1").orElseThrow();
        Long tagId = tag.getId();

        // when
        mockMvc.perform(delete("/tags/" + tagId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(tagRepository.findById(tagId).isEmpty());
    }
}