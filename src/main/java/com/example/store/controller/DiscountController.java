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
        List<ResponseDiscountDto> responseDiscountDtos = new ArrayList<>();

        discounts.forEach(discount -> {
            ResponseDiscountDto responseDiscountDto = ResponseDiscountDto.builder()
                    .discountName(discount.getDiscountName())
                    .discountPrice(discount.getDiscountPrice())
                    .quantity(discount.getQuantity())
                    .id(discount.getId())
                    .discountCondition(discount.getDiscountCondition())
                    .expirationDate(discount.getExpirationDate())
                    .build();

            responseDiscountDtos.add(responseDiscountDto);
        });

        return new ResponseEntity(responseDiscountDtos, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity allDiscount() {

        List<Discount> allDiscount = discountService.getAllDiscount();
        List<ResponseDiscountDto> responseDiscountDtos = new ArrayList<>();

        allDiscount.forEach(discount -> {
            ResponseDiscountDto responseDiscountDto = ResponseDiscountDto.builder()
                    .discountName(discount.getDiscountName())
                    .discountPrice(discount.getDiscountPrice())
                    .quantity(discount.getQuantity())
                    .id(discount.getId())
                    .discountCondition(discount.getDiscountCondition())
                    .expirationDate(discount.getExpirationDate())
                    .build();
            responseDiscountDtos.add(responseDiscountDto);
        });

        return new ResponseEntity(responseDiscountDtos, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity setDiscountByMember(@IfLogin LoginUserDto loginUserDto, @PathVariable(value = "id") Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        Discount discount = discountService.getDiscount(id);

        // 회원이 추가하려는 쿠폰이 이미 가지고 있는 쿠폰일 때
        if (member.getDiscounts().contains(discount)) {
            return null;
        }

        discount.updateQuantity(discount.getQuantity() - 1);

        member.addDiscount(discount);
        memberService.addMember(member);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity addDiscount(@IfLogin LoginUserDto loginUserDto, @RequestBody AddDiscountDto addDiscountDto) {

        Discount discount = Discount.builder()
                        .discountName(addDiscountDto.getDiscountName())
                        .discountPrice(addDiscountDto.getDiscountPrice())
                        .quantity(addDiscountDto.getQuantity())
                        .expirationDate(addDiscountDto.getExpirationDate())
                        .discountCondition(addDiscountDto.getDiscountCondition())
                        .build();

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

    // discount 삭제 안됨, 관련된 것까지 한번에 같이 삭제해야 함
    @DeleteMapping("/cancel/{id}")
    public void cancelDiscount(@IfLogin LoginUserDto loginUserDto, @PathVariable Long id) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
    }
}
