package com.msa.order.domain.settlement.repository;

import com.msa.order.domain.settlement.entity.Settlement;
import com.msa.order.domain.settlement.entity.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {

    List<SettlementItem> findBySettlement(Settlement settlement);
}
