package com.msa.member.domain.coupon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public String lIndex(String key, Long index) {
        return (String) redisTemplate.opsForList().index(key, index);
    }

    public void lPop(String key) {
        redisTemplate.opsForList().leftPop(key);
    }
}
