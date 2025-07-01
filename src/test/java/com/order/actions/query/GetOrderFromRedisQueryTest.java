package com.order.actions.query;

import com.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

public class GetOrderFromRedisQueryTest {
    @Mock
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private GetOrderFromRedisQuery getOrderFromRedisQuery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        org.springframework.test.util.ReflectionTestUtils.setField(getOrderFromRedisQuery, "orderCachePrefix", "order_cache_");
    }

    @Test
    void execute_shouldReturnOrder_whenOrderExistsInCache() {
        String orderId = "ORDER_1";
        String cacheKey = "order_cache_" + orderId;
        Order cachedOrder = new Order();

        when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(cachedOrder);

        Order result = getOrderFromRedisQuery.execute(orderId);

        assertSame(cachedOrder, result);
        verify(redisObjectTemplate.opsForValue()).get(cacheKey);
    }

    @Test
    void execute_shouldReturnNull_whenNoOrderInCache() {
        String orderId = "ORDER_2";
        String cacheKey = "order_cache_" + orderId;

        when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);

        Order result = getOrderFromRedisQuery.execute(orderId);

        assertNull(result);
        verify(redisObjectTemplate.opsForValue()).get(cacheKey);
    }

    @Test
    void execute_shouldReturnNull_whenCachedValueIsNotOrder() {
        String orderId = "ORDER_3";
        String cacheKey = "order_cache_" + orderId;

        when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn("some string");

        Order result = getOrderFromRedisQuery.execute(orderId);

        assertNull(result);
        verify(redisObjectTemplate.opsForValue()).get(cacheKey);
    }
}
