package com.msa.member.domain.delivery.service;

import com.msa.member.domain.delivery.dto.request.RequestDelivery;
import com.msa.member.domain.delivery.dto.response.ResponseDelivery;
import com.msa.member.domain.delivery.entity.Delivery;
import com.msa.member.domain.delivery.entity.DeliveryChecked;
import com.msa.member.domain.delivery.repository.DeliveryRepository;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.repository.MemberRepository;
import com.msa.member.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.msa.member.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository;

    public ResponseDelivery getDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        return ResponseDelivery.builder().request(delivery.getRequest()).phoneNumber(delivery.getPhoneNumber())
                .recipient(delivery.getRecipient()).address(delivery.getAddress()).checked(delivery.getDeliveryChecked()).build();
    }

    public List<ResponseDelivery> getDeliveries(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        List<ResponseDelivery> responseDeliveries = new ArrayList<>();
        List<Delivery> deliveries = member.getDeliveries();

        if (deliveries == null) {
            return null;
        }

        for (Delivery delivery : deliveries) {
            ResponseDelivery build = ResponseDelivery.builder().recipient(delivery.getRecipient()).address(delivery.getAddress())
                    .phoneNumber(delivery.getPhoneNumber()).request(delivery.getRequest()).checked(delivery.getDeliveryChecked()).build();
            responseDeliveries.add(build);
        }


        return responseDeliveries;
    }

    public void setDelivery(Long userId, RequestDelivery requestDelivery) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Delivery delivery = Delivery.builder()
                .address(requestDelivery.getAddress())
                .recipient(requestDelivery.getRecipient())
                .request(requestDelivery.getRequest())
                .member(member)
                .deliveryChecked(DeliveryChecked.UNCHECKED)
                .phoneNumber(requestDelivery.getPhoneNumber()).build();


        deliveryRepository.save(delivery);
        member.getDeliveries().add(delivery);
    }

    public void setDelivery(Long userId, RequestDelivery requestDelivery, Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        delivery.updateDeliver(
                requestDelivery.getAddress(), requestDelivery.getRecipient(),
                requestDelivery.getRequest(), requestDelivery.getPhoneNumber()
        );
    }

    public void deleteDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        deliveryRepository.delete(delivery);
    }

    public ResponseDelivery getDeliveryByIdChecked(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Delivery delivery = member.getDeliveries().stream()
                .filter(d -> d.getDeliveryChecked().equals(DeliveryChecked.CHECKED))
                .findFirst().get();

        return ResponseDelivery.builder().request(delivery.getRequest()).phoneNumber(delivery.getPhoneNumber())
                .recipient(delivery.getRecipient()).address(delivery.getAddress()).checked(delivery.getDeliveryChecked()).build();
    }

    public void updateDeliveryCheck(Long userId, Long id) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Delivery delivery = deliveryRepository.findById(id).orElseThrow();

        member.getDeliveries()
                .forEach(d -> {
                    if (d.getDeliveryChecked() == DeliveryChecked.CHECKED) {
                        d.setUnChecked();
                    }
                });

        delivery.setChecked();
    }

    public Long getDeliveryId(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Delivery delivery = member.getDeliveries().get(0);
        return delivery.getId();
    }
}
