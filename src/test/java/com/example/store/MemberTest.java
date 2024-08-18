package com.example.store;

import com.example.store.dto.MemberSignupDto;
import com.example.store.entity.Member;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
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
        MemberSignupDto memberSignupDto1 = MemberSignupDto.builder().email("wkadht0619@inu.ac.kr").name("aa").password("hhm0310@@").build();
        MemberSignupDto memberSignupDto2 = MemberSignupDto.builder().email("wkadht06191@inu.ac.kr").name("aa").password("hhm0310@@").build();

        memberService.createMember(memberSignupDto1);
        memberService.createMember(memberSignupDto2);

        Member memberByEmail1 = memberService.findByEmail(memberSignupDto1.getEmail());
        Member memberByEmail2 = memberService.findByEmail(memberSignupDto2.getEmail());

        Assertions.assertThat(memberSignupDto1.getEmail()).isEqualTo(memberByEmail1.getEmail());
    }
}