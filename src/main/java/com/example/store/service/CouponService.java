package com.example.store.service;

import com.example.store.dto.request.RequestCoupon;
import com.example.store.dto.response.ResponseCoupon;
import com.example.store.entity.Coupon;
import com.example.store.entity.Member;
import com.example.store.entity.MemberCoupon;
import com.example.store.exception.ex.couponException.CouponException;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.repository.CouponRepository;
import com.example.store.repository.MemberCouponRepository;
import com.example.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.store.exception.ex.ErrorCode.*;

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

    public Void issue(Long id, LoginUserDto loginUserDto) {
        Coupon coupon = getExistsCoupon(id); // 해당 쿠폰이 있는지 확인
        coupon.issue(); // 쿠폰수량, 발급기간을 확인

        Member member = memberRepository.findByEmail(loginUserDto.getEmail()).orElseThrow();

        saveMemberCoupon(member, coupon);

        return null;
    }

    public void issue(Long id, Long memberId) {
        Coupon coupon = getExistsCoupon(id); // 해당 쿠폰이 있는지 확인
        coupon.issue(); // 쿠폰수량, 발급기간을 확인

        Member member = memberRepository.findById(memberId).orElseThrow();

        saveMemberCoupon(member, coupon);

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
            throw new CouponException(NOT_FOUND_COUPON, "쿠폰 정책이 존재하지 않습니다. %s".formatted(id));
        });
    }

    private Coupon getExistsCoupon(Long id) {
        Coupon coupon = couponRepository.findByIdWithLock(id).orElseThrow(() -> new CouponException(NOT_FOUND_COUPON, "쿠폰이 존재하지 않습니다. %s".formatted(id)));
        return coupon;
    }

    private void alreadyExistsMemberCoupon(Member member, Coupon coupon) {
        if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            throw new CouponException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. member_id: %d, coupon_id: %d".formatted(member.getId(), coupon.getId()));
        }
    }

    private void existsByTitle(RequestCoupon requestCoupon) {
        if (couponRepository.existsByTitle(requestCoupon.getTitle())) {
            throw new CouponException(DUPLICATE_COUPON, "같은 제목의 쿠폰이 이미 존재합니다.. %s".formatted(requestCoupon.getTitle()));
        }
    }

}
