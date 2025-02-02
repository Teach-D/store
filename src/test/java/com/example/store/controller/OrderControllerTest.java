//package com.example.store.controller;
//
//import com.example.store.dto.request.*;
//import com.example.store.entity.*;
//import com.example.store.entity.Order;
//import com.example.store.repository.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@AutoConfigureMockMvc
//@DirtiesContext
//@Slf4j
//class OrderControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private CartRepository cartRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Autowired
//    private DeliveryRepository deliveryRepository;
//
//    private static String accessToken;
//
//    @BeforeAll
//    static void beforeLogin(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
//
//        // signup
//        RequestSignUp requestSignUp = RequestSignUp.builder()
//                .email("test1234@example.com")
//                .password("test1234!")
//                .name("test1234")
//                .build();
//
//        // When
//        mockMvc.perform(post("/members/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestSignUp)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        // login
//        RequestSignIn requestSignIn = RequestSignIn.builder()
//                .email("test1234@example.com")
//                .password("test1234!")
//                .build();
//
//        // When
//        ResultActions resultActions = mockMvc.perform(post("/members/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestSignIn)));
//
//        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
//        accessToken = JsonPath.parse(responseContent).read("$.result.accessToken");
//
//        // Then
//        resultActions.andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$.result.accessToken").value(accessToken));
//
//        // add category
//        // given
//        RequestCategory requestCategory1 = RequestCategory.builder()
//                .name("category1")
//                .build();
//
//        // When
//        mockMvc.perform(post("/categories")
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestCategory1)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        // add product
//        // given
//        RequestProduct requestProduct1 = RequestProduct.builder()
//                .title("product1")
//                .description("product1")
//                .price(10000)
//                .categoryId(1L)
//                .imageUrl(null)
//                .quantity(100)
//                .build();
//
//        // when
//        mockMvc.perform(post("/products")
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestProduct1)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        // add product
//        // given
//        RequestProduct requestProduct2 = RequestProduct.builder()
//                .title("product2")
//                .description("product2")
//                .price(20000)
//                .categoryId(1L)
//                .imageUrl(null)
//                .quantity(200)
//                .build();
//
//        // when
//        mockMvc.perform(post("/products")
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestProduct2)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        // Delivery 추가
//        // given
//        RequestDelivery requestDelivery1 = RequestDelivery.builder()
//                .recipient("recipientA")
//                .address("addressA")
//                .phoneNumber("phoneNumberA")
//                .request("requestA").build();
//
//        // When
//        mockMvc.perform(post("/deliveries")
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDelivery1)))
//                // then
//                .andExpect(status().isOk())
//                .andDo(print());
//    }
//
//    @Test
//    @org.junit.jupiter.api.Order(1)
//     void addOrderByDiscount() throws Exception {
//        // given
//        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
//        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
//        Long cartId = cart.getId();
//
//        Product product = productRepository.findByTitle("product1");
//        Long productId = product.getId();
//
//        RequestCartItem requestCartItem = RequestCartItem.builder().cartId(cartId).quantity(10).build();
//
//        // when
//        mockMvc.perform(post("/cartItems/" + productId)
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestCartItem)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        Coupon coupon = Coupon.builder()
//                .title("discount1")
//                .discountPrice(1000)
//                .expirationDate("2022.02.02")
//                .quantity(10)
//                .build();
//        couponRepository.save(coupon);
//
//
//        //given
//        Delivery delivery = Delivery.builder().deliveryChecked(DeliveryChecked.CHECKED).member(member).recipient("recipient1").build();
//        deliveryRepository.save(delivery);
//
//        // given
//        Coupon coupon1 = couponRepository.findByDiscountName("discount1").orElseThrow();
//        Long discount1Id = coupon1.getId();
//
//        // when
//        mockMvc.perform(post("/orders/" + discount1Id)
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//    }
//
//    @Test
//    @org.junit.jupiter.api.Order(2)
//    void addOrderByNoDiscount() throws Exception {
//        // given
//        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
//        Cart cart = cartRepository.findByMember(member).orElseThrow(RuntimeException::new);
//        Long cartId = cart.getId();
//
//        Product product = productRepository.findByTitle("product2");
//        Long productId = product.getId();
//
//        RequestCartItem requestCartItem = RequestCartItem.builder().cartId(cartId).quantity(10).build();
//
//        // when
//        mockMvc.perform(post("/cartItems/" + productId)
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestCartItem)))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        //given
//        Delivery delivery = Delivery.builder().deliveryChecked(DeliveryChecked.CHECKED).member(member).recipient("recipient1").build();
//        deliveryRepository.save(delivery);
//
//        // given
//        Coupon coupon1 = couponRepository.findByDiscountName("discount1").orElseThrow();
//        Long discount1Id = coupon1.getId();
//
//        // when
//        mockMvc.perform(post("/orders")
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//    }
//
//    @Test
//    @org.junit.jupiter.api.Order(3)
//    void getOrders() throws Exception {
//        // given
//        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get("/orders")
//                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // Then
//        resultActions.andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$.result[0].totalPrice").value(99000))
//                .andExpect(jsonPath("$.result[1].totalPrice").value(200000));
//    }
//
//    @Test
//    @org.junit.jupiter.api.Order(4)
//    void getOrder() throws Exception {
//        // given
//        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
//        Order order1 = orderRepository.findAllByMember(member).get(0);
//        Long orderId = order1.getOrderId();
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get("/orders/" + orderId)
//                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // Then
//        resultActions.andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$.result.totalPrice").value(99000));
//    }
//
//    @Test
//    @org.junit.jupiter.api.Order(5)
//    void deleteOrder() throws Exception {
//        // given
//        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow(RuntimeException::new);
//        Order order1 = orderRepository.findAllByMember(member).get(0);
//        Long orderId = order1.getOrderId();
//
//
//
//        // when
//        mockMvc.perform(delete("/orders/" + orderId)
//                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        // then
//        assertTrue(orderRepository.findById(orderId).isEmpty());
//
//        Product product = productRepository.findByTitle("product1");
//        assertEquals(product.getQuantity(), 100);
//    }
//}