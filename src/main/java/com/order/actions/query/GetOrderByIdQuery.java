package com.order.actions.query;

import com.order.entity.Order;
import com.order.exceptions.OrderNotFoundException;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetOrderByIdQuery {

    private final OrderRepository orderRepository;
    private final GetOrderFromRedisQuery getOrderFromCache;

    public Order execute(String orderId) {
        Order cachedOrder = getOrderFromCache.execute(orderId);
        if (cachedOrder != null) {
            log.info("[GetOrderByIdQuery] Getting Order {} from cache ", orderId);
            return cachedOrder;
        }

        log.info("[GetOrderByIdQuery] Getting Order {} from database ", orderId);
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("order.not.found"));
    }

}
