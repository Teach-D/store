package com.example.store.controller;

import com.example.store.dto.request.*;
import com.example.store.entity.Category;
import com.example.store.entity.Product;
import com.example.store.entity.Review;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@DirtiesContext
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static String accessToken;

    @Autowired
    private ReviewRepository reviewRepository;

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
    void addReview() throws Exception {
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        // given
        RequestReview requestReview1 = RequestReview.builder()
                        .rating(5)
                                .title("reviewA")
                                        .content("contentA").build();

        // When
        mockMvc.perform(post("/reviews/" + productId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestReview1)))
                .andExpect(status().isOk())
                .andDo(print());

        // given
        RequestReview requestReview2 = RequestReview.builder()
                .rating(5)
                .title("reviewB")
                .content("contentB").build();

        // When
        mockMvc.perform(post("/reviews/" + productId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestReview2)))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        Page<Review> reviewsPage = reviewRepository.findByProductId(productId, Pageable.unpaged());
        List<Review> content = reviewsPage.getContent();

        assertThat(content).hasSize(2);
        assertThat(content).extracting("title").containsExactlyInAnyOrder("reviewA", "reviewB");
        assertThat(content).extracting("product.id").containsOnly(productId);

    }

    @Test
    @Order(2)
    void getReview() throws Exception {
        // given
        Review review = reviewRepository.findByTitle("reviewA").orElseThrow();
        Long reviewId = review.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/reviews/" + reviewId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.title").value("reviewA"));
    }

    @Test
    @Order(3)
    void getReviewsByProduct() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        // when
        mockMvc.perform(get("/reviews")
                        .param("productId", productId.toString())
                        .param("page", "0")
                        .param("sort", "title")
                        .param("order", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content", hasSize(2)))
                .andExpect(jsonPath("$.result.content[0].title").value("reviewA"))
                .andExpect(jsonPath("$.result.content[1].title").value("reviewB"))
                .andDo(print());
    }

    @Test
    @Order(4)
    void editReview() throws Exception {
        // given
        Review review = reviewRepository.findByTitle("reviewA").orElseThrow();
        Long reviewId = review.getId();

        RequestReview requestReview = RequestReview.builder().title("reviewA-update").build();

        // when
        ResultActions resultActions = mockMvc.perform(patch("/reviews/" + reviewId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestReview)));

        // then
        try {
            resultActions.andExpect(status().isOk())
                    .andDo(print());

            Review updateReview = reviewRepository.findById(reviewId).orElseThrow();
            assertEquals("reviewA-update", updateReview.getTitle());
        } catch (AssertionError e) {
            Review currentReview = reviewRepository.findById(reviewId).orElseThrow();
            assertEquals("reviewA", currentReview.getTitle(), "데이터베이스의 상태가 변경되지 않아야 합니다.");

            throw e;
        }
    }

    @Test
    @Order(5)
    void deleteProduct() throws Exception {
        // given
        Review review = reviewRepository.findByTitle("reviewA-update").orElseThrow();
        Long reviewId = review.getId();

        // when
        mockMvc.perform(delete("/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(reviewRepository.findById(reviewId).isEmpty());
    }
}