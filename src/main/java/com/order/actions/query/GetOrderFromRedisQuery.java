package com.order.actions.query;

import com.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetOrderFromRedisQuery {

    private final RedisTemplate<String, Object> redisObjectTemplate;

    @Value("${cache.redis.order.prefix}")
    private String orderCachePrefix;

    public Order execute(String orderId) {
        try {
            String cacheKey = orderCachePrefix + orderId;
            Object cachedValue = redisObjectTemplate.opsForValue().get(cacheKey);

            if (cachedValue instanceof Order) {
                return (Order) cachedValue;
            }

            return null;
        } catch (Exception e) {
            log.warn("[GetOrderFromRedisQuery] error.getting.order.from.redis");
            log.warn("[GetOrderFromRedisQuery] order {} ", orderId, e);
            return null;
        }
    }
}
