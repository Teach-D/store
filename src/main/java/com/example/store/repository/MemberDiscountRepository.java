package com.example.store.repository;

import com.example.store.entity.Member;
import com.example.store.entity.MemberDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDiscountRepository extends JpaRepository<MemberDiscount, Long> {
    void deleteByMemberAndDiscount_id(Member member, Long id);
    void deleteByDiscount_id(Long id);
}
