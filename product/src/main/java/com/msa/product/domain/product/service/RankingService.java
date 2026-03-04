package com.msa.product.domain.product.service;

import com.msa.product.domain.product.dto.response.RankingEntry;
import com.msa.product.domain.product.dto.response.RankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final long DAILY_TTL_DAYS   = 7;
    private static final long WEEKLY_TTL_DAYS  = 90;
    private static final long MONTHLY_TTL_DAYS = 365;

    public void incrementRanking(Long productId, int quantity) {
        String member = "product:" + productId;
        String dailyKey = getDailyKey();
        String weeklyKey = getWeeklyKey();
        String monthlyKey = getMonthlyKey();

        incrementAndSetTTL(dailyKey, member, quantity, DAILY_TTL_DAYS);
        incrementAndSetTTL(weeklyKey, member, quantity, WEEKLY_TTL_DAYS);
        incrementAndSetTTL(monthlyKey, member, quantity, MONTHLY_TTL_DAYS);
    }

    public RankingResponse getRanking(String period, int limit) {
        String key = getKeyByPeriod(period);
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        List<RankingEntry> entries = new ArrayList<>();
        if (tuples != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                entries.add(RankingEntry.builder()
                        .rank(rank++)
                        .productId(parseProductId(Objects.requireNonNull(tuple.getValue())))
                        .score(tuple.getScore() != null ? tuple.getScore() : 0.0)
                        .build());
            }
        }

        return RankingResponse.builder()
                .period(period)
                .key(key)
                .entries(entries)
                .build();
    }


    private void incrementAndSetTTL(String key, String member, double score, long ttlDays) {
        stringRedisTemplate.opsForZSet().incrementScore(key, member, score);
        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            stringRedisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
        }
    }

    private Long parseProductId(String member) {
        return Long.parseLong(member.replace("product:", ""));
    }

    private String getKeyByPeriod(String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> getDailyKey();
            case "weekly" -> getWeeklyKey();
            case "monthly" -> getMonthlyKey();
            default -> throw new IllegalArgumentException("지원하지 않는 랭킹 기간: " + period);
        };
    }

    private String getDailyKey() {
        return "ranking:daily:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String getWeeklyKey() {
        LocalDate now = LocalDate.now();
        int week = now.get(WeekFields.ISO.weekOfWeekBasedYear());
        return "ranking:weekly:" + now.getYear() + "-W" + String.format("%02d", week);
    }

    private String getMonthlyKey() {
        return "ranking:monthly:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
