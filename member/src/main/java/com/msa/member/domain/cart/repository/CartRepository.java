package com.msa.member.domain.cart.repository;

import com.msa.member.domain.cart.entity.Cart;
import com.msa.member.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository  extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMember(Member member);
}