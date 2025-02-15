/*

package com.example.store.controller;

import com.example.store.dto.request.*;
import com.example.store.entity.Coupon;
import com.example.store.entity.Member;
import com.example.store.entity.MemberCoupon;
import com.example.store.repository.CouponRepository;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
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
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    private static String accessToken;

    @Autowired
    private ReviewRepository reviewRepository;
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
    void addDiscount() throws Exception {
        // given
        RequestCoupon requestCoupon1 = RequestCoupon.builder()
                .title("discount1")
                .discountAmount(10000)
                .expirationDate("2024.04.01")
                .quantity(10)
                .build();

        // When
        mockMvc.perform(post("/discounts")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCoupon1)))
                .andExpect(status().isOk())
                .andDo(print());

        // given
        RequestCoupon requestCoupon2 = RequestCoupon.builder()
                .discountName("discount2")
                .discountPrice(20000)
                .expirationDate("2024.04.02")
                .quantity(20)
                .build();

        // When
        mockMvc.perform(post("/discounts")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCoupon2)))
                .andExpect(status().isOk())
                .andDo(print());

        // given
        RequestCoupon requestCoupon3 = RequestCoupon.builder()
                .discountName("discount3")
                .discountPrice(30000)
                .expirationDate("2024.04.03")
                .quantity(30)
                .build();

        // When
        mockMvc.perform(post("/discounts")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCoupon3)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(2)
    void setDiscountByMember() throws Exception {
        // given1
        Coupon coupon1 = couponRepository.findByDiscountName("discount1").orElseThrow();
        Long discount1Id = coupon1.getId();

        // when1
        mockMvc.perform(post("/discounts/" + discount1Id)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // given2
        Coupon coupon2 = couponRepository.findByDiscountName("discount2").orElseThrow();
        Long discount2Id = coupon2.getId();

        // when2
        mockMvc.perform(post("/discounts/" + discount2Id)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        Coupon updateCoupon1 = couponRepository.findByDiscountName("discount1").orElseThrow();
        Coupon updateCoupon2 = couponRepository.findByDiscountName("discount2").orElseThrow();

        assertEquals(updateCoupon1.getQuantity(), coupon1.getQuantity()-1);
        assertEquals(updateCoupon2.getQuantity(), coupon2.getQuantity()-1);
    }

    @Test
    @Order(3)
    @Transactional
    void getAllDiscountByMember() throws Exception {

        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow();
        List<MemberCoupon> discounts = member.getDiscounts();
        log.info(discounts.toString());


        // when
        mockMvc.perform(get("/discounts")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].discountName").value("discount1"))
                .andExpect(jsonPath("$.result[1].discountName").value("discount2"))
                .andDo(print());
    }

    @Test
    @Order(4)
    @Transactional
    void getAllDiscount() throws Exception {
        // when
        mockMvc.perform(get("/discounts/all")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(3)))
                .andExpect(jsonPath("$.result[0].discountName").value("discount1"))
                .andExpect(jsonPath("$.result[1].discountName").value("discount2"))
                .andExpect(jsonPath("$.result[2].discountName").value("discount3"))
                .andDo(print());
    }

    @Test
    @Order(5)
    void editDiscount() throws Exception {
        // given
        Coupon coupon = couponRepository.findByDiscountName("discount1").orElseThrow();
        Long discountId = coupon.getId();

        RequestCoupon requestCoupon = RequestCoupon.builder()
                .discountName("update-discount1")
                .discountPrice(2000)
                .expirationDate("2024.10.10")
                .quantity(100)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/discounts/" + discountId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCoupon)));

        // then
        try {
            resultActions.andExpect(status().isOk())
                    .andDo(print());

            Coupon updateCoupon = couponRepository.findById(discountId).orElseThrow();
            assertEquals("update-discount1", updateCoupon.getDiscountName());
        } catch (AssertionError e) {
            Coupon currentCoupon = couponRepository.findById(discountId).orElseThrow();
            assertEquals("discount1", currentCoupon.getDiscountName(), "데이터베이스의 상태가 변경되지 않아야 합니다.");

            throw e;
        }
    }

    @Test
    @Order(6)
    @Transactional
    void cancelDiscount() throws Exception {
        // given
        Coupon coupon = couponRepository.findByDiscountName("update-discount1").orElseThrow();
        Long discountId = coupon.getId();

        Member member = memberRepository.findByEmail("test1234@example.com").orElseThrow();

        // when
        mockMvc.perform(delete("/discounts/" + discountId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertFalse(member.getDiscounts().contains(coupon));
    }

    @Test
    @Order(7)
    void deleteDiscount() throws Exception {
        // given
        Coupon coupon = couponRepository.findByDiscountName("update-discount1").orElseThrow();
        Long discountId = coupon.getId();;

        // when
        mockMvc.perform(delete("/discounts/" + discountId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(couponRepository.findById(discountId).isEmpty());
    }
}
*/
