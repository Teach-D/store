package com.example.store.controller;

import com.example.store.dto.AddDeliveryDto;
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

        ResponseDeliveryDto responseDeliveryDto = new ResponseDeliveryDto();
        responseDeliveryDto.setAddress(delivery.getAddress());
        responseDeliveryDto.setRecipient(delivery.getRecipient());
        responseDeliveryDto.setRequest(delivery.getRequest());
        responseDeliveryDto.setPhoneNumber(delivery.getPhoneNumber());
        log.info(responseDeliveryDto.toString());
        return responseDeliveryDto;
    }

    @PostMapping
    public void setDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
        String email = loginUserDto.getEmail();
        log.info(addDeliveryDto.getRequest());
        log.info(email);
        Member member = memberService.findByEmail(email);
        Delivery delivery = new Delivery();
        delivery.setAddress(addDeliveryDto.getAddress());
        delivery.setRecipient(addDeliveryDto.getRecipient());
        delivery.setRequest(addDeliveryDto.getRequest());
        delivery.setPhoneNumber(addDeliveryDto.getPhoneNumber());
        deliveryService.addDelivery(delivery);
        member.setDelivery(delivery);
        memberService.addMember(member);
    }

    @PutMapping
    public void updateDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Delivery delivery = member.getDelivery();
        delivery.setAddress(addDeliveryDto.getAddress());
        delivery.setRecipient(addDeliveryDto.getRecipient());
        delivery.setRequest(addDeliveryDto.getRequest());
        delivery.setPhoneNumber(addDeliveryDto.getPhoneNumber());
        deliveryService.updateDelivery(delivery);
    }

    @DeleteMapping
    public void deleteDelivery(@IfLogin LoginUserDto loginUserDto) {
        String email = loginUserDto.getEmail();
        Member member = memberService.findByEmail(email);
        Delivery delivery = member.getDelivery();
        member.setDelivery(null);

        deliveryService.deleteDelivery(delivery);
    }

}
