package com.example.store.service;

import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Member;
import com.example.store.entity.MemberDiscount;
import com.example.store.repository.MemberDiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberDiscountService {

    private final MemberDiscountRepository memberDiscountRepository;

    public void save(MemberDiscount memberDiscount) {
        memberDiscountRepository.save(memberDiscount);
    }

    public ResponseEntity<SuccessDto> cancelDiscount(Member member, Long id) {
        memberDiscountRepository.deleteByMemberAndDiscount_id(member, id);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));

    }
}
