package com.example.store.service;

import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseProduct;
import com.example.store.entity.Category;
import com.example.store.entity.Product;
import com.example.store.entity.ProductTag;
import com.example.store.entity.Tag;
import com.example.store.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Slf4j
public class ProductCacheServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private TagRepository tagRepository;

    @Autowired
    private ProductCacheService productCacheService;

    private ValueOperations<String, Object> valueOperations;
    private ValueOperations<String, String> stringValueOperations;

    private Long tagId = 1L;
    private String redisKey = "tag:popular:" + tagId;
    private Category category;
    private Product product;
    private Tag tag;
    ProductTag productTag;

    @BeforeEach
    void setUp() {
        valueOperations = redisTemplate.opsForValue();
        stringValueOperations = stringRedisTemplate.opsForValue();
        clearRedis(); // Redis 초기화
        clearDatabase(); // DB 초기화
        setObject(); // 객체 초기화
    }

    @AfterEach
    void tearDown() {
        clearRedis(); // Redis 데이터 삭제
        clearDatabase(); // DB 데이터 삭제
    }

    private void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    private void clearDatabase() {
        tagRepository.deleteAll();
    }

    private void setObject() {
        // Mock 데이터 생성
        category = Category.builder().name("category1").build();
        product = Product.builder().id(1L).title("product1").category(category).build();
        tag = Tag.builder().id(tagId).build();

        productTag = ProductTag.builder().product(product).tag(tag).build();
        tag.getProductTags().add(productTag);

        // TagRepository 모킹
        when(tagRepository.findById(tagId)).thenReturn(of(tag));
    }

    @Test
    void testGetPopularProductsByTagId_withoutCache_viewCountBelowThreshold() {
        // Redis 캐시 초기화
        valueOperations.getOperations().delete(redisKey);
        stringValueOperations.set("tag:viewcount:" + tagId.toString(), "5");

        // 테스트 실행
        ResponseDto<List<ResponseProduct>> response = productCacheService.getPopularProductsByTagId(tagId);
        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(1, response.getResult().size());

        assertNull(valueOperations.get(redisKey));
    }

    @Test
    void testGetPopularProductsByTagId_withoutCache_viewCountUpThreshold() {
        // Redis 캐시 초기화
        valueOperations.getOperations().delete(redisKey);
        stringValueOperations.set("tag:viewcount:" + tagId.toString(), "9");

        // 테스트 실행
        ResponseDto<List<ResponseProduct>> response = productCacheService.getPopularProductsByTagId(tagId);
        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(1, response.getResult().size());

        assertNotNull(valueOperations.get(redisKey));
    }

    @Test
    void testRemoveCache() {
        List<Product> cachedProducts = Collections.singletonList(product);
        valueOperations.set(redisKey, cachedProducts, 1L, TimeUnit.HOURS); // Redis에 데이터 저장

        stringValueOperations.set("tag:viewcount:" + tagId.toString(), "8"); // View Count 설정

        // Redis에 데이터가 정상적으로 저장되었는지 확인
        assertNotNull(valueOperations.get(redisKey));

        // 인기 제품 조회 (Redis 캐시와 동작 확인)
        ResponseDto<List<ResponseProduct>> response = productCacheService.getPopularProductsByTagId(tagId);
        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(1, response.getResult().size());

        // removeUnPopularTagAndProducts 호출
        productCacheService.removeUnPopularTagAndProducts();

        // Redis에서 해당 키가 삭제되었는지 확인
        assertNull(valueOperations.get(redisKey));
    }

    @Test
    void testIncrementTagViewCountWithRedis() {
        String countKey = "tag:viewcount:" + tagId;

        // 초기 상태 확인
        assertNull(stringRedisTemplate.opsForValue().get(countKey));

        // 메서드 호출
        productCacheService.getPopularProductsByTagId(tagId);

        // 값 확인
        String value = stringRedisTemplate.opsForValue().get(countKey);
        assertNotNull(value);
        assertEquals("1", value);

        // TTL 확인
        Long ttl = stringRedisTemplate.getExpire(countKey, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl <= 86400 && ttl > 0);
    }
}
