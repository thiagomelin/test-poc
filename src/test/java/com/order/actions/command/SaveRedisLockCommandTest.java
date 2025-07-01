package com.order.actions.command;

import com.order.exceptions.DuplicateOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SaveRedisLockCommandTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SaveRedisLockCommand saveRedisLockCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        org.springframework.test.util.ReflectionTestUtils.setField(saveRedisLockCommand, "orderLockPrefix", "order_cache_");
        org.springframework.test.util.ReflectionTestUtils.setField(saveRedisLockCommand, "lockTimeoutSeconds", 60);
    }

    @Test
    void execute_shouldSetLockSuccessfully() {
        String externalId = "ORDER_123";
        String lockKey = "order_cache_ORDER_123";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(lockKey, "locked", 60, TimeUnit.SECONDS)).thenReturn(true);

        assertDoesNotThrow(() -> saveRedisLockCommand.execute(externalId));

        verify(redisTemplate.opsForValue()).setIfAbsent(lockKey, "locked", 60, TimeUnit.SECONDS);
    }

    @Test
    void execute_shouldThrowDuplicateOrderExceptionWhenLockExists() {
        String externalId = "ORDER_DUP";
        String lockKey = "order_cache_ORDER_DUP";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(lockKey, "locked", 60, TimeUnit.SECONDS)).thenReturn(false);

        DuplicateOrderException ex = assertThrows(DuplicateOrderException.class, () -> saveRedisLockCommand.execute(externalId));

        assertTrue(ex.getMessage().contains("ORDER_DUP"));

        verify(redisTemplate.opsForValue()).setIfAbsent(lockKey, "locked", 60, TimeUnit.SECONDS);
    }

    @Test
    void releaseLock_shouldDeleteLockKey() {
        String externalId = "ORDER_RELEASE";
        String lockKey = "order_cache_ORDER_RELEASE";

        saveRedisLockCommand.releaseLock(externalId);

        verify(redisTemplate).delete(lockKey);
    }
}
