package com.example.store.service;

import com.example.store.dto.AddDiscountDto;
import com.example.store.dto.ResponseDiscountDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Discount;
import com.example.store.entity.Member;
import com.example.store.entity.MemberDiscount;
import com.example.store.exception.ex.DeliveryException.NotFoundDeliveryException;
import com.example.store.exception.ex.DiscountException.NotFoundDiscountException;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.DiscountRepository;
import com.example.store.repository.MemberDiscountRepository;
import com.example.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final MemberRepository memberRepository;
    private final MemberDiscountRepository memberDiscountRepository;

    public void addDiscount(Discount discount) {
        discountRepository.save(discount);
    }

    public Discount getDiscount(Long discountId) {
        return discountRepository.findById(discountId).get();
    }

    public ResponseDto<List<ResponseDiscountDto>> getAllDiscount() {

        List<Discount> allDiscount = discountRepository.findAll();
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

        return ResponseDto.success(responseDiscountDtos);
    }

    public ResponseEntity<SuccessDto> setDiscountByMember(LoginUserDto loginUserDto, Long id) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);
        Discount discount = discountRepository.findById(id).orElseThrow(NotFoundDiscountException::new);

        // 회원이 추가하려는 쿠폰이 이미 가지고 있는 쿠폰일 때
        if (member.getDiscounts().contains(discount)) {
            return null;
        }

        discount.updateQuantity(discount.getQuantity() - 1);

        member.addDiscount(discount);
        memberDiscountRepository.save(new MemberDiscount(member, discount));
        memberRepository.save(member);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteDiscount(Long discountId) {
        discountRepository.deleteById(discountId);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> editDiscount(Long discountId, AddDiscountDto addDiscountDto) {
        Discount discount = discountRepository.findById(discountId).get();

        discount.updateDiscount(
                addDiscountDto.getDiscountName(),
                addDiscountDto.getDiscountPrice(),
                addDiscountDto.getExpirationDate(),
                addDiscountDto.getQuantity()
        );

        discountRepository.save(discount);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseDto<List<ResponseDiscountDto>> getAllDiscountByMember(LoginUserDto loginUserDto) {

        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundDeliveryException::new);

        List<Discount> discounts = new ArrayList<>();
        List<MemberDiscount> memberDiscounts = member.getDiscounts();

        for (MemberDiscount memberDiscount : memberDiscounts) {
            Discount discount = memberDiscount.getDiscount();
            discounts.add(discount);
        }

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

        return ResponseDto.success(responseDiscountDtos);
    }

    public ResponseEntity<SuccessDto> addDiscount(LoginUserDto loginUserDto, AddDiscountDto addDiscountDto) {
        Discount discount = Discount.builder()
                .discountName(addDiscountDto.getDiscountName())
                .discountPrice(addDiscountDto.getDiscountPrice())
                .quantity(addDiscountDto.getQuantity())
                .expirationDate(addDiscountDto.getExpirationDate())
                .discountCondition(addDiscountDto.getDiscountCondition())
                .build();

        discountRepository.save(discount);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
