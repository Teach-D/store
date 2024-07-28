package com.example.store.service;

import com.example.store.dto.AddDeliveryDto;
import com.example.store.dto.ResponseDeliveryDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Delivery;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MemberRepository memberRepository;

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

    public ResponseDto<ResponseDeliveryDto> getDelivery(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = member.getDelivery();

        if (delivery == null) {
            return null;
        }

        ResponseDeliveryDto responseDeliveryDto = ResponseDeliveryDto
                .builder().address(delivery.getAddress())
                .recipient(delivery.getRecipient())
                .request(delivery.getRequest())
                .phoneNumber(delivery.getPhoneNumber()).build();

        return ResponseDto.success(responseDeliveryDto);
    }

    public ResponseEntity<SuccessDto> setDelivery(LoginUserDto loginUserDto, AddDeliveryDto addDeliveryDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = Delivery.builder()
                .address(addDeliveryDto.getAddress())
                .recipient(addDeliveryDto.getRecipient())
                .request(addDeliveryDto.getRequest())
                .phoneNumber(addDeliveryDto.getPhoneNumber()).build();

        deliveryRepository.save(delivery);
        member.addDelivery(delivery);
        //memberRepository.save(member);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));

    }

    public ResponseEntity<SuccessDto> updateDelivery(LoginUserDto loginUserDto, AddDeliveryDto addDeliveryDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = member.getDelivery();
        delivery.updateDeliver(
                addDeliveryDto.getAddress(), addDeliveryDto.getRecipient(),
                addDeliveryDto.getRequest(), addDeliveryDto.getPhoneNumber()
        );

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteDelivery(LoginUserDto loginUserDto) {
        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(NotFoundMemberException::new);

        Delivery delivery = member.getDelivery();
        member.emptyDelivery();

        deliveryRepository.delete(delivery);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
