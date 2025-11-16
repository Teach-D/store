package com.msa.member.domain.coupon.service;

import com.msa.member.domain.coupon.dto.request.RequestCoupon;
import com.msa.member.domain.coupon.dto.response.ResponseCoupon;
import com.msa.member.domain.coupon.entity.Coupon;
import com.msa.member.domain.coupon.repository.CouponRepository;
import com.msa.member.domain.member.entity.Member;
import com.msa.member.domain.member.entity.MemberCoupon;
import com.msa.member.domain.coupon.repository.MemberCouponRepository;
import com.msa.member.domain.member.repository.MemberRepository;
import com.msa.member.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.msa.member.global.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;

    public List<ResponseCoupon> getAllCoupons() {
        List<Coupon> allCoupons = couponRepository.findAll();

        return  allCoupons.stream().map(ResponseCoupon::of).toList(); // entity -> dto
    }

    public ResponseCoupon getCoupon(Long id) {
        Coupon coupon = getExistsCoupon(id);

        return ResponseCoupon.of(coupon);
    }

    public ResponseCoupon saveCoupon(RequestCoupon requestCoupon) {
        existsByTitle(requestCoupon);
        Coupon coupon = RequestCoupon.dtoToEntity(requestCoupon);

        Coupon savedCoupon = couponRepository.save(coupon);

        return ResponseCoupon.of(savedCoupon);
    }

    public ResponseCoupon updateCoupon(Long id, RequestCoupon requestCoupon) {
        existsByTitle(requestCoupon);
        Coupon coupon = getExistsCoupon(id);

        coupon.update(requestCoupon);

        return ResponseCoupon.of(coupon);
    }

    public Void deleteCoupon(Long id) {
        Coupon coupon = getExistsCoupon(id);

        couponRepository.delete(coupon);

        return null;
    }

    public Void issue(Long couponId, Long userId) {
        Coupon coupon = getExistsCoupon(couponId); // 해당 쿠폰이 있는지 확인
        coupon.issue(); // 쿠폰수량, 발급기간을 확인

        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        saveMemberCoupon(member, coupon);

        return null;
    }


    public Void issueLoadTest(Long couponId, Long memberId) {
        Coupon coupon = getExistsCoupon(couponId); // 해당 쿠폰이 있는지 확인
        coupon.issue(); // 쿠폰수량, 발급기간을 확인

        Member member = memberRepository.findById(memberId).orElseThrow();
        saveMemberCoupon(member, coupon);

        return null;
    }

    private void saveMemberCoupon(Member member, Coupon coupon) {
        alreadyExistsMemberCoupon(member, coupon); // 이미 유저가 발급받았는지 확인

        MemberCoupon memberCoupon = new MemberCoupon(member, coupon);

        memberCouponRepository.save(memberCoupon);
        coupon.addMemberCoupon(memberCoupon); // orphanremoval을 위한 양방향 매핑
    }

    public Coupon findCoupon(Long id) {
        return couponRepository.findById(id).orElseThrow(() -> {
            throw new CustomException(NOT_FOUND_COUPON);
        });
    }

    private Coupon getExistsCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_COUPON));
        return coupon;
    }

    private void alreadyExistsMemberCoupon(Member member, Coupon coupon) {
        if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            throw new CustomException(DUPLICATED_COUPON_ISSUE);
        }
    }

    private void existsByTitle(RequestCoupon requestCoupon) {
        if (couponRepository.existsByTitle(requestCoupon.getTitle())) {
            throw new CustomException(DUPLICATE_COUPON);
        }
    }

    public int getDiscountAmount(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CustomException(NOT_FOUND_COUPON));
        return coupon.getDiscountAmount();
    }
}
