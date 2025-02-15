package com.example.store.repository;

import com.example.store.entity.Member;
import com.example.store.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    public List<Order> findAllByMember(Member member);
    public List<Order> findAllByMember_Id(Long memberId);
}
