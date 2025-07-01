package com.order.resources.response;

import com.order.entity.Order;
import com.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
    String id,
    String externalId,
    String customerId,
    BigDecimal totalAmount,
    OrderStatus status,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderItemResponse> items
) {

    public static OrderResponse fromOrder(Order order){
        return new OrderResponse(
                order.getId(),
                order.getExternalId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getProcessedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems() != null ? order.getItems().stream()
                        .map(OrderItemResponse::fromOrderItem)
                        .collect(Collectors.toList()) : null
        );
    }
}