package com.order.actions.command;

import com.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class AddOrderToCacheCommandTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AddOrderToCacheCommand addOrderToCacheCommand;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        addOrderToCacheCommand = new AddOrderToCacheCommand(redisTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(addOrderToCacheCommand, "orderCachePrefix", "order_cache_");
        org.springframework.test.util.ReflectionTestUtils.setField(addOrderToCacheCommand, "cacheTtlHours", 2);
    }

    @Test
    void execute_shouldAddOrderToCacheSuccessfully() {
        Order order = new Order();
        order.setExternalId("12345");
        addOrderToCacheCommand.execute(order);
        verify(valueOperations, times(1)).set("order_cache_12345", order, 2, TimeUnit.HOURS);
    }

    @Test
    void execute_shouldLogErrorWhenExceptionOccurs() {
        Order order = new Order();
        order.setExternalId("67890");
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis error"));
        addOrderToCacheCommand.execute(order);
        verify(redisTemplate, times(1)).opsForValue();
    }
}
