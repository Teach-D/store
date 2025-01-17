package com.example.store.controller;

import com.example.store.dto.request.*;
import com.example.store.entity.*;
import com.example.store.entity.Tag;
import com.example.store.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
@Slf4j
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

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
    void addCartItem() throws Exception {
        // given
        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
        Long cartId = cart.getId();

        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        RequestCartItem requestCartItem = RequestCartItem.builder().cartId(cartId).quantity(10).build();

        // when
        mockMvc.perform(post("/cartItems/" + productId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCartItem)))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        Product updateProduct = productRepository.findById(productId).orElseThrow(RuntimeException::new);

        assertEquals(updateProduct.getSaleQuantity(), 10);
        assertEquals(updateProduct.getQuantity(), 90);
    }

    @Test
    @Order(2)
    void getCartItem() throws Exception {
        // given
        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
        Long cartId = cart.getId();

        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId).orElseThrow(RuntimeException::new);
        Long cartItemId = cartItem.getId();

        log.info("--------");
        log.info(cartItem.toString());

        // When
        ResultActions resultActions = mockMvc.perform(get("/cartItems/" + cartItemId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.quantity").value(10))
                .andExpect(jsonPath("$.result.productId").value(productId));
    }

    @Test
    @Order(3)
    void getCartItems() throws Exception {
        // given
        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
        Long cartId = cart.getId();

        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/cartItems")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result[0].productId").value(productId))
                .andExpect(jsonPath("$.result[0].quantity").value(10));
    }


    @Test
    @Order(4)
    void updateCartItemQuantity() throws Exception {
        // given
        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
        Long cartId = cart.getId();

        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId).orElseThrow(RuntimeException::new);
        Long cartItemId = cartItem.getId();

        int quantity = 20;

        // When
        ResultActions resultActions = mockMvc.perform(put("/cartItems/" + cartItemId + "/" + quantity)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        // then
        Product updateProduct = productRepository.findById(productId).orElseThrow(RuntimeException::new);

        assertEquals(updateProduct.getSaleQuantity(), quantity);
        assertEquals(updateProduct.getQuantity(), 80);
    }

    @Test
    @Order(5)
    void deleteCartItem() throws Exception {
        // given
        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
        Long cartId = cart.getId();

        Product product = productRepository.findByTitle("product1");
        Long productId = product.getId();

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId).orElseThrow(RuntimeException::new);
        Long cartItemId = cartItem.getId();

        // when
        mockMvc.perform(delete("/cartItems/" + cartItemId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(cartItemRepository.findById(cartItemId).isEmpty());
    }
}