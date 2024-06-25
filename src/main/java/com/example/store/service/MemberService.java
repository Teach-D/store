package com.example.store.service;

import com.example.store.entity.Member;
import com.example.store.entity.Role;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }

    @Transactional
    public Member addMember(Member member) {
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        member.addRole(userRole.get());
        Member saveMember = memberRepository.save(member);
        return saveMember;
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMember(String email) {
        return memberRepository.findByEmail(email);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
