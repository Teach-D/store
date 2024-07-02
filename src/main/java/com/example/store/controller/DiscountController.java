package com.example.store.controller;

import com.example.store.dto.AddDiscountDto;
import com.example.store.dto.ResponseDiscountDto;
import com.example.store.entity.Discount;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.DiscountService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/discount")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DiscountController {

    private final DiscountService discountService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity allDiscountByMember(@IfLogin LoginUserDto loginUserDto) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        List<Discount> discounts = member.getDiscounts();
        log.info("aa");
        for (Discount discount : discounts) {
            log.info(discount.toString());
        }



        List<ResponseDiscountDto> responseDiscountDtos = new ArrayList<>();
        for (Discount discount : discounts) {
            ResponseDiscountDto responseDiscountDto = new ResponseDiscountDto();
            responseDiscountDto.setDiscountName(discount.getDiscountName());
            responseDiscountDto.setDiscountPrice(discount.getDiscountPrice());
            responseDiscountDto.setQuantity(discount.getQuantity());
            responseDiscountDto.setId(discount.getId());
            responseDiscountDto.setDiscountCondition(discount.getDiscountCondition());
            responseDiscountDto.setExpirationDate(discount.getExpirationDate());
            responseDiscountDtos.add(responseDiscountDto);
        }
        return new ResponseEntity(responseDiscountDtos, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity allDiscount(@IfLogin LoginUserDto loginUserDto) {
        List<Discount> allDiscount = discountService.getAllDiscount();
        List<ResponseDiscountDto> responseDiscountDtos = new ArrayList<>();
        for (Discount discount : allDiscount) {
            ResponseDiscountDto responseDiscountDto = new ResponseDiscountDto();
            responseDiscountDto.setDiscountName(discount.getDiscountName());
            responseDiscountDto.setDiscountPrice(discount.getDiscountPrice());
            responseDiscountDto.setQuantity(discount.getQuantity());
            responseDiscountDto.setId(discount.getId());
            responseDiscountDto.setDiscountCondition(discount.getDiscountCondition());
            responseDiscountDto.setExpirationDate(discount.getExpirationDate());
            responseDiscountDtos.add(responseDiscountDto);
        }
        return new ResponseEntity(responseDiscountDtos, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity setDiscountByMember(@IfLogin LoginUserDto loginUserDto, @PathVariable(value = "id") Long id) {

        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Discount discount = discountService.getDiscount(id);

        if (member.getDiscounts().contains(discount)) {
            return null;
        }

        discount.setQuantity(discount.getQuantity() - 1);
        member.addDiscount(discount);
        for (Discount memberDiscount : member.getDiscounts()) {
            log.info(memberDiscount.getDiscountName());
        }
        memberService.addMember(member);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity addDiscount(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDiscountDto addDiscountDto) {
        Discount discount = new Discount();
        discount.setDiscountName(addDiscountDto.getDiscountName());
        discount.setDiscountPrice(addDiscountDto.getDiscountPrice());
        discount.setQuantity(addDiscountDto.getQuantity());
        discount.setExpirationDate(addDiscountDto.getExpirationDate());
        discount.setDiscountCondition(addDiscountDto.getDiscountCondition());
        discountService.addDiscount(discount);
        return new ResponseEntity(HttpStatus.OK);

    }

    @PutMapping("/{id}")
    public void editDiscount(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDiscountDto addDiscountDto, @PathVariable Long id) {
       discountService.updateDiscount(id, addDiscountDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/cancel/{id}")
    public void cancelDiscount(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

    }
}
