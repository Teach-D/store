package com.example.store.service;

import com.example.store.dto.request.RequestCoupon;
import com.example.store.dto.response.ResponseCoupon;
import com.example.store.entity.Coupon;
import com.example.store.entity.CouponType;
import com.example.store.exception.ex.couponException.CouponException;
import com.example.store.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static com.example.store.exception.ex.ErrorCode.DUPLICATE_COUPON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    public CouponServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveCoupon_Success() {
        // given
        RequestCoupon requestCoupon = RequestCoupon.builder()
                .title("설날 특별 쿠폰")
                .type("FIRST_COME_FIRST_SERVED")
                .totalQuantity(100)
                .discountAmount(5000)
                .minAvailableAmount(20000)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(5))
                .build();

        Coupon expectedCoupon = Coupon.builder()
                .title(requestCoupon.getTitle())
                .type(CouponType.parsing(requestCoupon.getType()))
                .totalQuantity(requestCoupon.getTotalQuantity())
                .discountAmount(requestCoupon.getDiscountAmount())
                .minAvailableAmount(requestCoupon.getMinAvailableAmount())
                .dateIssueStart(requestCoupon.getDateIssueStart())
                .dateIssueEnd(requestCoupon.getDateIssueEnd())
                .build();

        // Mock 설정
        given(couponRepository.existsByTitle(requestCoupon.getTitle())).willReturn(false); // 중복되지 않은 제목
        given(couponRepository.save(any(Coupon.class))).willReturn(expectedCoupon);

        // when
        ResponseCoupon responseCoupon = couponService.saveCoupon(requestCoupon);

        // then
        assertNotNull(responseCoupon);
        assertEquals(requestCoupon.getTitle(), responseCoupon.getTitle());
        assertEquals(requestCoupon.getDiscountAmount(), responseCoupon.getDiscountAmount());
        assertEquals(requestCoupon.getTotalQuantity(), responseCoupon.getTotalQuantity());

        // couponRepository.save() 호출 확인
        verify(couponRepository, times(1)).save(any(Coupon.class));
        verify(couponRepository, times(1)).existsByTitle(requestCoupon.getTitle());
    }

    @Test
    void saveCoupon_DuplicateTitle() {
        // given
        RequestCoupon requestCoupon = RequestCoupon.builder()
                .title("설날 특별 쿠폰") // 중복된 제목
                .type("FIRST_COME_FIRST_SERVED")
                .totalQuantity(100)
                .discountAmount(5000)
                .minAvailableAmount(20000)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(5))
                .build();

        // Mock 설정
        given(couponRepository.existsByTitle(requestCoupon.getTitle())).willReturn(true); // 중복된 제목

        // when
        CouponException couponException = assertThrows(CouponException.class, () -> {
            couponService.saveCoupon(requestCoupon);
        });

        // then
        assertEquals(DUPLICATE_COUPON, couponException.getErrorCode());
        assertTrue(couponException.getMessage().contains("같은 제목의 쿠폰이 이미 존재합니다"));
        assertTrue(couponException.getMessage().contains(requestCoupon.getTitle()));

        // couponRepository.save()가 호출되지 않았는지 확인
        verify(couponRepository, never()).save(any(Coupon.class));
        verify(couponRepository, times(1)).existsByTitle(requestCoupon.getTitle());
    }
}
