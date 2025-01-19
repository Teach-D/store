package com.example.store.controller;

import com.example.store.dto.request.RequestDelivery;
import com.example.store.dto.request.RequestSignIn;
import com.example.store.dto.request.RequestSignUp;
import com.example.store.entity.Delivery;
import com.example.store.entity.DeliveryChecked;
import com.example.store.repository.DeliveryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.*;
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
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;

    @Autowired
    private DeliveryRepository deliveryRepository;

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
    void setDelivery() throws Exception {
        // given
        RequestDelivery requestDelivery1 = RequestDelivery.builder()
                        .recipient("recipientA")
                                .address("addressA")
                                        .phoneNumber("phoneNumberA")
                                                .request("requestA").build();

        // When
        mockMvc.perform(post("/deliveries")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDelivery1)))
                // then
                .andExpect(status().isOk())
                .andDo(print());

        // given
        RequestDelivery requestDelivery2 = RequestDelivery.builder()
                .recipient("recipientB")
                .address("addressB")
                .phoneNumber("phoneNumberB")
                .request("requestB").build();

        // When
        mockMvc.perform(post("/deliveries")
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDelivery2)))
                // then
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    @Order(2)
    void getDeliveriesByMember() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(get("/deliveries")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result[0].recipient").value("recipientA"))
                .andExpect(jsonPath("$.result[1].recipient").value("recipientB"));
    }

    @Test
    @Order(3)
    void getDeliveryById() throws Exception {
        // given
        Delivery delivery = deliveryRepository.findByRecipient("recipientA").orElseThrow();
        Long deliveryId = delivery.getId();

        // When
        ResultActions resultActions = mockMvc.perform(get("/deliveries/" + deliveryId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.recipient").value("recipientA"));
    }

    @Test
    @Order(4)
    void updateDeliveryChecked() throws Exception {
        // given
        Delivery delivery = deliveryRepository.findByRecipient("recipientA").orElseThrow();
        Long deliveryId = delivery.getId();

        // When
        ResultActions resultActions = mockMvc.perform(patch("/deliveries/check/" + deliveryId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());
        assertThat(deliveryRepository.findByRecipient("recipientA").get().getDeliveryChecked()).isEqualTo(DeliveryChecked.CHECKED);
    }

    @Test
    @Order(5)
    void getDeliveryByIdChecked() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(get("/deliveries/check")
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.result.recipient").value("recipientA"));
    }


    @Test
    @Order(6)
    void updateDelivery() throws Exception {
        // given
        Delivery delivery = deliveryRepository.findByRecipient("recipientA").orElseThrow();
        Long deliveryId = delivery.getId();

        Delivery updateDelivery = Delivery.builder()
                .recipient("recipientA-update")
                .address("addressA-update")
                .build();

        // When
        ResultActions resultActions = mockMvc.perform(patch("/deliveries/" + deliveryId)
                .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDelivery)));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        assertThat(deliveryRepository.findById(deliveryId).get().getRecipient()).isEqualTo("recipientA-update");
        assertThat(deliveryRepository.findById(deliveryId).get().getAddress()).isEqualTo("addressA-update");
    }

    @Test
    @Order(7)
    void deleteDelivery() throws Exception {
        // given
        Delivery delivery = deliveryRepository.findByRecipient("recipientA-update").orElseThrow();
        Long deliveryId = delivery.getId();

        // when
        mockMvc.perform(delete("/deliveries/" + deliveryId)
                        .header("Authorization", "Bearer " + accessToken) // 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertTrue(deliveryRepository.findById(deliveryId).isEmpty());
    }
}