package com.order.actions.command;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exceptions.DuplicateOrderException;
import com.order.repository.OrderRepository;
import com.order.resources.request.OrderRequest;
import com.order.resources.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderCommand {

    private final OrderRepository orderRepository;
    private final AddOrderToCacheCommand addOrderToCacheCommand;
    private final SaveRedisLockCommand saveRedisLockCommand;
    private final ValidateIfOrderIsDuplicated validateIfOrderIsDuplicated;

    public OrderResponse receiveOrder(OrderRequest request) {
        log.info("[CreateOrderCommand] Starting order process: {}", request.externalId());

        saveRedisLockCommand.execute(request.externalId());
        Order order = request.toOrder();
        try {
            validateIfOrderIsDuplicated.execute(request.externalId());

            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.SUCCESS);

            Order savedOrder = orderRepository.save(order);
            log.info("[CreateOrderCommand] Order created: {}", request.externalId());

            addOrderToCacheCommand.execute(order);

            return OrderResponse.fromOrder(savedOrder);

        } catch (DuplicateOrderException exc){
            log.error("[CreateOrderCommand] error.duplicated.order");
            return null;
        } catch (Exception e) {
            log.error("[CreateOrderCommand] error.saving.order");
            log.error("[CreateOrderCommand] Error created: {}", request.externalId());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            return null;
        } finally {
            saveRedisLockCommand.releaseLock(request.externalId());
        }
    }
} 