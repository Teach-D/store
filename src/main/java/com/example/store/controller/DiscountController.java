package com.example.store.controller;

import com.example.store.dto.AddDiscountDto;
import com.example.store.dto.ResponseDiscountDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Discount;
import com.example.store.entity.Member;
import com.example.store.entity.MemberDiscount;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DiscountService;
import com.example.store.service.MemberDiscountService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseDto<List<ResponseDiscountDto>> getAllDiscountByMember(@IfLogin LoginUserDto loginUserDto){
        return discountService.getAllDiscountByMember(loginUserDto);
}

    @GetMapping("/all")
    public ResponseDto<List<ResponseDiscountDto>> getAllDiscount() {
        return discountService.getAllDiscount();
    }

    @PostMapping("/{id}")
    public ResponseEntity<SuccessDto> setDiscountByMember(@IfLogin LoginUserDto loginUserDto, @PathVariable(value = "id") Long id) {
        return discountService.setDiscountByMember(loginUserDto, id);
    }

    @PostMapping
    public ResponseEntity<SuccessDto> addDiscount(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDiscountDto addDiscountDto) {
        return discountService.addDiscount(loginUserDto, addDiscountDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessDto> editDiscount(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDiscountDto addDiscountDto, @PathVariable Long id) {
       return discountService.editDiscount(id, addDiscountDto);
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
