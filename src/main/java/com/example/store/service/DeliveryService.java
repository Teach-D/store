package com.example.store.service;

import com.example.store.dto.request.RequestDelivery;
import com.example.store.dto.response.ResponseDelivery;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Delivery;
import com.example.store.entity.DeliveryChecked;
import com.example.store.entity.Member;
import com.example.store.exception.ex.MemberException.NotFoundMemberException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.DeliveryRepository;
import com.example.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository;

    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    public void addDelivery(Delivery delivery) {
        deliveryRepository.save(delivery);
    }

    public ResponseDto<ResponseDelivery> getDelivery(Member member, Delivery delivery) {
        ResponseDelivery responseDelivery = ResponseDelivery.builder().request(delivery.getRequest()).phoneNumber(delivery.getPhoneNumber())
                .recipient(delivery.getRecipient()).address(delivery.getAddress()).checked(delivery.getDeliveryChecked()).build();
        return ResponseDto.success(responseDelivery);
    }

    public void updateDelivery(Delivery delivery) {
        deliveryRepository.save(delivery);
    }

    public void deleteDelivery(Delivery delivery) {
        deliveryRepository.delete(delivery);
    }

    public ResponseDto<List<ResponseDelivery>> getDeliveries(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

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


        return ResponseDto.success(responseDeliveries);
    }

    public ResponseEntity<SuccessDto> setDelivery(LoginUserDto loginUserDto, RequestDelivery requestDelivery) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = Delivery.builder()
                .address(requestDelivery.getAddress())
                .recipient(requestDelivery.getRecipient())
                .request(requestDelivery.getRequest())
                .member(member)
                .deliveryChecked(DeliveryChecked.UNCHECKED)
                .phoneNumber(requestDelivery.getPhoneNumber()).build();


        deliveryRepository.save(delivery);
        member.getDeliveries().add(delivery);
        //memberRepository.save(member);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));

    }

    public ResponseEntity<SuccessDto> updateDelivery(LoginUserDto loginUserDto, RequestDelivery requestDelivery, Long id) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        delivery.updateDeliver(
                requestDelivery.getAddress(), requestDelivery.getRecipient(),
                requestDelivery.getRequest(), requestDelivery.getPhoneNumber()
        );

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteDelivery(LoginUserDto loginUserDto, Long id) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = deliveryRepository.findById(id).orElseThrow();
        deliveryRepository.delete(delivery);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseDto<ResponseDelivery> getDeliveryByIdChecked(Member member) {
        Delivery delivery = member.getDeliveries().stream()
                .filter(d -> d.getDeliveryChecked().equals(DeliveryChecked.CHECKED))
                .findFirst().get();

        ResponseDelivery responseDelivery = ResponseDelivery.builder().request(delivery.getRequest()).phoneNumber(delivery.getPhoneNumber())
                .recipient(delivery.getRecipient()).address(delivery.getAddress()).checked(delivery.getDeliveryChecked()).build();

        return ResponseDto.success(responseDelivery);
    }

    public ResponseEntity updateDeliveryCheck(Member member, Long id) {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow();

        member.getDeliveries()
                .forEach(d -> {
                    if (d.getDeliveryChecked() == DeliveryChecked.CHECKED) {
                        d.setUnChecked();
                    }
                });

        delivery.setChecked();

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
