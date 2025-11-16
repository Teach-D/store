package com.msa.member.domain.delivery.repository;

import com.msa.member.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
