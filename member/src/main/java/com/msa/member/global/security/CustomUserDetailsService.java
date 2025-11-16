package com.msa.member.global.security;


import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new UsernameNotFoundException("id [" + id + "] 에 해당하는 사용자를 찾을 수 없습니다."));
        return new CustomUserDetails(member);
    }
}