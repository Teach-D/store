package com.msa.product.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "${member.service.url}")
public interface MemberServiceClient {

    @GetMapping("/members/{memberId}/gender")
    String getMemberGender(@PathVariable("memberId") Long memberId);

    @GetMapping("/members/{memberId}/birth-date")
    String getMemberBirthDate(@PathVariable("memberId") Long memberId);
}
