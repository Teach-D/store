package com.example.store.repository;

import com.example.store.entity.Cart;
import com.example.store.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository  extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMemberAndDate(Member member, String date);
    Optional<Cart> findByMember(Member member);
}