package com.order.actions.command;

import com.order.exceptions.DuplicateOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveRedisLockCommand {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.redis.lock.prefix}")
    private String orderLockPrefix;

    @Value("${cache.redis.lock.ttl-seconds}")
    private int lockTimeoutSeconds;

    public void execute(String externalId){

        String lockKey = orderLockPrefix + externalId;

        log.warn("[SaveRedisLockCommand] lock.register {}", externalId);
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", lockTimeoutSeconds, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("[SaveRedisLockCommand] error.order.locked");
            throw new DuplicateOrderException("Order is locked: " + externalId);
        }
    }

    public void releaseLock(String externalId){
        String lockKey = orderLockPrefix + externalId;
        redisTemplate.delete(lockKey);
    }

}
