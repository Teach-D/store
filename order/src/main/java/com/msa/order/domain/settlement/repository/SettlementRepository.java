package com.msa.order.domain.settlement.repository;

import com.msa.order.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySellerIdAndSettlementDate(Long sellerId, LocalDate date);

    boolean existsBySellerIdAndSettlementDate(Long sellerId, LocalDate date);
}
