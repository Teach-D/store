package com.example.store.service;

import com.example.store.dto.AddDiscountDto;
import com.example.store.entity.Discount;
import com.example.store.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;

    public void addDiscount(Discount discount) {
        discountRepository.save(discount);
    }

    public Discount getDiscount(Long discountId) {
        return discountRepository.findById(discountId).get();
    }

    public List<Discount> getAllDiscount() {
        return discountRepository.findAll();
    }

    public void deleteDiscount(Long discountId) {
        discountRepository.deleteById(discountId);
    }

    public void updateDiscount(Long discountId, AddDiscountDto addDiscountDto) {
        Discount discount = discountRepository.findById(discountId).get();
        discount.setDiscountName(addDiscountDto.getDiscountName());
        discount.setDiscountPrice(addDiscountDto.getDiscountPrice());
        discount.setExpirationDate(addDiscountDto.getExpirationDate());
        discount.setQuantity(addDiscountDto.getQuantity());
        discountRepository.save(discount);
    }
}
