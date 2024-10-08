package com.example.store.controller;

import com.example.store.dto.request.RequestDelivery;
import com.example.store.dto.response.ResponseDelivery;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Delivery;
import com.example.store.entity.Member;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DeliveryService;
import com.example.store.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final MemberService memberService;

    @GetMapping
    public ResponseDto<List<ResponseDelivery>> getDeliveriesByMember(@IfLogin LoginUserDto loginUserDto) {
        return deliveryService.getDeliveries(loginUserDto);
    }

    @GetMapping("/{id}")
    public ResponseDto<ResponseDelivery> getDeliveryById(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Delivery delivery = deliveryService.getDeliveryById(id);

        return deliveryService.getDelivery(member, delivery);
    }

    @GetMapping("/check")
    public ResponseDto<ResponseDelivery> getDeliveryByIdChecked(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return deliveryService.getDeliveryByIdChecked(member);
    }

    @PostMapping
    public ResponseEntity setDelivery(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestDelivery requestDelivery, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        return deliveryService.setDelivery(loginUserDto, requestDelivery);
    }

    @PatchMapping("/check/{id}")
    public ResponseEntity updateDeliveryChecked(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return deliveryService.updateDeliveryCheck(member, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateDelivery(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestDelivery requestDelivery, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return deliveryService.updateDelivery(loginUserDto, requestDelivery, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteDelivery(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        return deliveryService.deleteDelivery(loginUserDto, id);
    }

}
