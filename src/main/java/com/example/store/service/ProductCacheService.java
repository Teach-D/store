package com.example.store.service;

import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.entity.Product;
import com.example.store.entity.ProductTag;
import com.example.store.entity.Tag;
import com.example.store.repository.TagRepository;
import com.example.store.utils.ResponseProductListUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductCacheService {

    private static final String TAG_POPULAR_PRODUCTS_KEY = "tag:popular:";
    private static final long TTL = 24 * 60 * 60; // 24시간
    private static final int cacheLeastCount = 3;

    private final RedisTemplate<String, Object> redisTemplate;
    private final TagRepository tagRepository;
    private final StringRedisTemplate stringRedisTemplate;

    /*
    태그 조회수 증가, 하루에 100 조회수를 넘으면 Redis에 product list 저장
    */
    public ResponseDto<List<ResponseProduct>> getPopularProductsByTagId(Long tagId) {
        // 각 product list의 key
        String key = TAG_POPULAR_PRODUCTS_KEY + tagId;

        log.info("key: {}", key);

        List<Product> cachedProducts = getCachedProducts(key);

        log.info("increment");

        // tag가 조회되었다고 cached에서 조회수를 1씩 증가
        incrementTagViewCount(tagId);

        // redis에 tagId에 연관되어 있는 product list가 있는 경우
        if (cachedProducts != null) {
            log.info("cahedProducts: {}", cachedProducts);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<Product> productList = objectMapper.convertValue(cachedProducts, new TypeReference<List<Product>>() {});

            return ResponseDto.success(ResponseProductListUtils.productListToResponseProductList(productList));
        }

        // DB에서 tagId로 product list 조회
        Tag tag = tagRepository.findById(tagId).get();
        List<ProductTag> productTags = tag.getProductTags();
        List<Product> products = new ArrayList<>();

        for (ProductTag productTag : productTags) {
            Product product = productTag.getProduct();
            products.add(product);
        }

        if (getDailyTagViewCount(tagId) >= 10) {
            log.info("dailyTagViewCount: {}", getDailyTagViewCount(tagId));
            setCacheProductList(key, products);
        }

        log.info("result: {}", products);

        return ResponseDto.success(ResponseProductListUtils.productListToResponseProductList(products));
    }

    // 하루동안 조회수가 100회가 넘어서 cache에 product list 저장
    private void setCacheProductList(String key, List<Product> products) {
        redisTemplate.opsForValue().set(key, products, TTL, TimeUnit.SECONDS);
    }

    // 하루동안 tagId가 조회된 횟수 조회
    private int getDailyTagViewCount(Long tagId) {
        String countKey = "tag:viewcount:" + tagId;
        String viewCount = stringRedisTemplate.opsForValue().get(countKey);
        return viewCount == null ? 0 : Integer.parseInt(viewCount);
    }

    // tagId를 조회하면 조회했다는 redis count가 1씩 증가
    private void incrementTagViewCount(Long tagId) {
        String countKey = "tag:viewcount:" + tagId;
        Long incrementCount = stringRedisTemplate.opsForValue().increment(countKey);
        log.info("incrementCount: {}", incrementCount);
        // 하루 동안에 tagId의 viewCount를 유지
        if (incrementCount == 1L) {
            stringRedisTemplate.expire(countKey, TTL, TimeUnit.SECONDS); // 24시간 유지
        }
    }

    // redis에 적재된 key가 파라미터 "key"인 product list
    private List<Product> getCachedProducts(String key) {
        List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(key);
        return cachedProducts;
    }

    // 매일 00시 00분에 조회수가 cacheLeastCount 미만이면 Redis에서 삭제
    @Scheduled(cron = "0 0 0 * * *")
    public void removeUnPopularTagAndProducts() {
        Set<String> keys = stringRedisTemplate.keys(TAG_POPULAR_PRODUCTS_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                String tagId = key.replace(TAG_POPULAR_PRODUCTS_KEY, "");
                if (getDailyTagViewCount(Long.valueOf(tagId)) < cacheLeastCount) {
                    redisTemplate.delete(key);
                    stringRedisTemplate.delete("tag:viewcount:" + tagId);
                }
            }
        }
    }

}