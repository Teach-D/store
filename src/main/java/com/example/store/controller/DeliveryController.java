package com.example.store.controller;

import com.example.store.dto.request.RequestDelivery;
import com.example.store.dto.response.ResponseDelivery;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
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

@Slf4j
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final MemberService memberService;

    @GetMapping
    public ResponseDto<ResponseDelivery> getDelivery(@IfLogin LoginUserDto loginUserDto) {
        return deliveryService.getDelivery(loginUserDto);
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

    @PutMapping
    public ResponseEntity updateDelivery(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestDelivery requestDelivery, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return deliveryService.updateDelivery(loginUserDto, requestDelivery);
    }

    @DeleteMapping
    public ResponseEntity<SuccessDto> deleteDelivery(@IfLogin LoginUserDto loginUserDto) {
        return deliveryService.deleteDelivery(loginUserDto);
    }

}
