package com.msa.member.domain.cart.repository;

import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.member.entity.Member;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository  extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMember(Member member);

    Optional<Cart> findByMemberId(Long userId);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItemList WHERE c.member.id = :memberId")
    Optional<Cart> findByMemberIdWithItems(@Param("memberId") Long memberId);
}