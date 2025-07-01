package com.order.resources.response;

import com.order.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
    String productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {

    public static OrderItemResponse fromOrderItem(OrderItem item){
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}