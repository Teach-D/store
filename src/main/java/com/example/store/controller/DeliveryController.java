package com.example.store.controller;

import com.example.store.dto.AddDeliveryDto;
import com.example.store.dto.ResponseCartItemDto;
import com.example.store.dto.ResponseDeliveryDto;
import com.example.store.entity.Delivery;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DeliveryService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final MemberService memberService;

    @GetMapping
    public ResponseDeliveryDto getCart(@IfLogin LoginUserDto loginUserDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Delivery delivery = member.getDelivery();

        if (delivery == null) {
            return null;
        }

        ResponseDeliveryDto responseDeliveryDto = ResponseDeliveryDto
                .builder().address(delivery.getAddress())
                        .recipient(delivery.getRecipient())
                                .request(delivery.getRequest())
                                        .phoneNumber(delivery.getPhoneNumber()).build();
        return responseDeliveryDto;
    }

    @PostMapping
    public void setDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Delivery delivery = Delivery.builder()
                .address(addDeliveryDto.getAddress())
                        .recipient(addDeliveryDto.getRecipient())
                                .request(addDeliveryDto.getRequest())
                                        .phoneNumber(addDeliveryDto.getPhoneNumber()).build();
        deliveryService.addDelivery(delivery);
        member.addDelivery(delivery);
        memberService.addMember(member);
    }

    @PutMapping
    public void updateDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Delivery delivery = member.getDelivery();
        delivery.updateDeliver(
                addDeliveryDto.getAddress(), addDeliveryDto.getRecipient(),
                addDeliveryDto.getRequest(), addDeliveryDto.getPhoneNumber()
        );

        deliveryService.updateDelivery(delivery);
    }

    @DeleteMapping
    public void deleteDelivery(@IfLogin LoginUserDto loginUserDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Delivery delivery = member.getDelivery();
        member.emptyDelivery();

        deliveryService.deleteDelivery(delivery);
    }

}
