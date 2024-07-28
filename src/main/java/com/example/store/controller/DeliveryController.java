package com.example.store.controller;

import com.example.store.dto.*;
import com.example.store.entity.Delivery;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DeliveryService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final MemberService memberService;

    @GetMapping
    public ResponseDto<ResponseDeliveryDto> getDelivery(@IfLogin LoginUserDto loginUserDto) {
        return deliveryService.getDelivery(loginUserDto);
    }

    @PostMapping
    public ResponseEntity<SuccessDto> setDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
         return deliveryService.setDelivery(loginUserDto, addDeliveryDto);
    }

    @PutMapping
    public ResponseEntity<SuccessDto> updateDelivery(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDeliveryDto addDeliveryDto) {
        return deliveryService.updateDelivery(loginUserDto, addDeliveryDto);
    }

    @DeleteMapping
    public ResponseEntity<SuccessDto> deleteDelivery(@IfLogin LoginUserDto loginUserDto) {
        return deliveryService.deleteDelivery(loginUserDto);
    }

}
