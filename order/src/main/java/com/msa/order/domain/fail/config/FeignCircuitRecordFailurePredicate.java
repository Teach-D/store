package com.msa.order.domain.fail.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@Slf4j
public class FeignCircuitRecordFailurePredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (!(throwable instanceof FeignException)) {
            log.debug("Non-FeignException 발생 - 실패로 기록: {}",
                    throwable.getClass().getSimpleName());
            return true;
        }

        FeignException feignException = (FeignException) throwable;
        int status = feignException.status();

        // 4XX 클라이언트 에러는 실패로 기록하지 않음
        if (status >= 400 && status < 500) {
            log.debug("4XX 클라이언트 에러 - 실패로 기록 안함: {}", status);
            return false;
        }

        // 5XX 서버 에러, Timeout, Connection 에러 등은 실패로 기록
        log.debug("5XX 서버 에러 또는 네트워크 에러 - 실패로 기록: {}", status);
        return true;
    }
}
