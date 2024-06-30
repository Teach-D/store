package com.example.store.service;

import com.example.store.entity.Delivery;
import com.example.store.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public void addDelivery(Delivery delivery) {
        deliveryRepository.save(delivery);
    }

    public Delivery getDelivery(Delivery delivery) {
        return deliveryRepository.findById(delivery.getId()).get();
    }

    public void updateDelivery(Delivery delivery) {
        deliveryRepository.save(delivery);
    }

    public void deleteDelivery(Delivery delivery) {
        deliveryRepository.delete(delivery);
    }

}
