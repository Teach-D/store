package com.msa.product.domain.product.scheduler;

import com.msa.product.domain.product.entity.AgeGroup;
import com.msa.product.domain.product.entity.Gender;
import com.msa.product.domain.product.service.ProductStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsWriteBehindScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductStatsService productStatsService;

    @Scheduled(fixedDelay = 10000) // 10초마다 DB 반영
    public void flushStatsToDB() {
        Set<String> dirtyKeys = stringRedisTemplate.opsForSet().members("stats:order:dirty");
        if (dirtyKeys == null || dirtyKeys.isEmpty()) return;

        log.info("[Write-Behind] 플러시 시작: {}건", dirtyKeys.size());

        for (String key : dirtyKeys) {
            try {
                String[] parts = key.split(":");
                Long productId = Long.parseLong(parts[0]);
                Gender gender  = Gender.valueOf(parts[1]);
                AgeGroup age   = AgeGroup.valueOf(parts[2]);

                // 값을 읽으면서 동시에 삭제 — 중복 반영 방지
                String countStr    = stringRedisTemplate.opsForValue().getAndDelete("stats:order:" + key + ":count");
                String quantityStr = stringRedisTemplate.opsForValue().getAndDelete("stats:order:" + key + ":quantity");

                int count    = countStr    != null ? Integer.parseInt(countStr)    : 0;
                int quantity = quantityStr != null ? Integer.parseInt(quantityStr) : 0;

                if (count == 0 && quantity == 0) {
                    stringRedisTemplate.opsForSet().remove("stats:order:dirty", key);
                    continue;
                }

                productStatsService.flushOrderStats(productId, gender, age, count, quantity);
                stringRedisTemplate.opsForSet().remove("stats:order:dirty", key);

            } catch (Exception e) {
                log.error("[Write-Behind] 플러시 실패 (다음 스케줄 재시도): key={}", key, e);
                // dirty set에 그대로 남겨두어 다음 스케줄에 재시도
            }
        }

        log.info("[Write-Behind] 플러시 완료");
    }
}
