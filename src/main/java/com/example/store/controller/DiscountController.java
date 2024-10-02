package com.example.store.controller;

import com.example.store.dto.request.RequestDiscount;
import com.example.store.dto.response.ResponseDiscount;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DiscountService;
import com.example.store.service.MemberDiscountService;
import com.example.store.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/discounts")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DiscountController {

    private final DiscountService discountService;
    private final MemberService memberService;
    private final MemberDiscountService memberDiscountService;

    @GetMapping
    public ResponseDto<List<ResponseDiscount>> getAllDiscountByMember(@IfLogin LoginUserDto loginUserDto){
        return discountService.getAllDiscountByMember(loginUserDto);
}

    @GetMapping("/all")
    public ResponseDto<List<ResponseDiscount>> getAllDiscount() {
        return discountService.getAllDiscount();
    }

    @PostMapping("/{id}")
    public ResponseEntity<SuccessDto> setDiscountByMember(@IfLogin LoginUserDto loginUserDto, @PathVariable(value = "id") Long id) {
        return discountService.setDiscountByMember(loginUserDto, id);
    }

    @PostMapping
    public ResponseEntity addDiscount(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestDiscount requestDiscount, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return discountService.addDiscount(loginUserDto, requestDiscount);
    }

    @PutMapping("/{id}")
    public ResponseEntity editDiscount(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestDiscount requestDiscount, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return discountService.editDiscount(id, requestDiscount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteDiscount(@PathVariable Long id) {
        return discountService.deleteDiscount(id);
    }

    // discount 삭제 안됨, 관련된 것까지 한번에 같이 삭제해야 함
    @DeleteMapping("/users/{id}")
    public ResponseEntity<SuccessDto> cancelDiscount(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return memberDiscountService.cancelDiscount(member, id);
    }
}
