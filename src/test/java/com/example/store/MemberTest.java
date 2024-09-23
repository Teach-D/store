package com.example.store;

import com.example.store.dto.request.RequestSignUp;
import com.example.store.entity.Member;
import com.example.store.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberTest {

    @Autowired
    private MemberService memberService;



    @Test
    @DisplayName("멤버 저장하고 조회하기")
    public void saveAndFind() {
        RequestSignUp requestSignUp1 = RequestSignUp.builder().email("wkadht0619@inu.ac.kr").name("aa").password("hhm0310@@").build();
        RequestSignUp requestSignUp2 = RequestSignUp.builder().email("wkadht06191@inu.ac.kr").name("aa").password("hhm0310@@").build();

        memberService.createMember(requestSignUp1);
        memberService.createMember(requestSignUp2);

        Member memberByEmail1 = memberService.findByEmail(requestSignUp1.getEmail());
        Member memberByEmail2 = memberService.findByEmail(requestSignUp2.getEmail());

        Assertions.assertThat(requestSignUp1.getEmail()).isEqualTo(memberByEmail1.getEmail());
    }
}