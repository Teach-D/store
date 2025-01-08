package com.example.store.controller;

import com.example.store.dto.request.RequestCategory;
import com.example.store.dto.request.RequestProduct;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.entity.Category;
import com.example.store.entity.Product;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.ProductRepository;
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static String accessToken;

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
    @Order(1)
    void addProduct() throws Exception {
        // given
        RequestProduct requestProduct1 = RequestProduct.builder()
                .title("product1")
                .description("product1")
                .price(10000)
                .categoryId(1L)
                .imageUrl(null)
                .quantity(100)
                .build();

        RequestProduct requestProduct2 = RequestProduct.builder()
                .title("product2")
                .description("product2")
                .price(20000)
                .categoryId(1L)
                .imageUrl(null)
                .quantity(200)
                .build();

        RequestProduct requestProduct3 = RequestProduct.builder()
                .title("product3")
                .description("product3")
                .price(30000)
                .categoryId(2L)
                .imageUrl(null)
                .quantity(300)
                .build();

        RequestProduct requestProduct4 = RequestProduct.builder()
                .title("product4")
                .description("product4")
                .price(40000)
                .categoryId(2L)
                .imageUrl(null)
                .quantity(400)
                .build();

        // when
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestProduct1)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestProduct2)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestProduct3)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestProduct4)))
                .andExpect(status().isOk())
                .andDo(print());


    }

    @Test
    @Order(2)
    void getProductsByOption() throws Exception {
        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + ProductControllerTest.accessToken)
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("sort", "price")
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.content[0].product.title").value("product1"))
                .andExpect(jsonPath("$.result.content[1].product.title").value("product2"));
    }

    @Test
    @Order(3)
    void getProduct() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/products/" + productId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.product.title").value("product1"));

    }

    @Test
    @Order(4)
    void getProductByName() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1");
        String title = product.getTitle();

        // When
        ResultActions resultActions = mockMvc.perform(get("/products/name/" + title)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.product.title").value("product1"))
                .andExpect(jsonPath("$.result.product.price").value("10000"));
    }

    @Test
    @Order(5)
    void editProduct() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        RequestProduct requestProduct = RequestProduct.builder()
                .title("product1-1")
                .description("product1-1")
                .price(11000)
                .categoryId(2L)
                .imageUrl(null)
                .quantity(110)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(patch("/products/" + productId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestProduct)));

        // then
        try {
            resultActions.andExpect(status().isOk())
                    .andDo(print());

            Product updateProduct = productRepository.findById(productId).orElseThrow();
            assertEquals("product1-1", updateProduct.getTitle());
        } catch (AssertionError e) {
            Product currentProduct = productRepository.findById(productId).orElseThrow();
            assertEquals("product1", currentProduct.getTitle(), "데이터베이스의 상태가 변경되지 않아야 합니다.");

            throw e;
        }
    }

    @Test
    @Order(6)
    void deleteProduct() throws Exception {
        // given
        Product product = productRepository.findByTitle("product1-1");
        Long productId = product.getId();

        // when
        mockMvc.perform(delete("/products/" + productId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(productRepository.findById(productId).isEmpty());
    }
}