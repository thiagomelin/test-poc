package com.order.actions.command;

import com.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddOrderToCacheCommand {

    private final RedisTemplate<String, Object> redisObjectTemplate;

    @Value("${cache.redis.order.prefix}")
    private String orderCachePrefix;

    @Value("${cache.redis.order.ttl-hours}")
    private int cacheTtlHours;

    public void execute(Order order) {
        try {
            String cacheKey = orderCachePrefix + order.getExternalId();
            redisObjectTemplate.opsForValue().set(cacheKey, order, cacheTtlHours, TimeUnit.HOURS);
            log.info("[AddOrderToCacheCommand] Added order {} to Cache", order.getExternalId());
        } catch (Exception e) {
            log.error("[AddOrderToCacheCommand] error.adding.order.redis");
            log.error("[AddOrderToCacheCommand] Error: ", e);
        }
    }

}
