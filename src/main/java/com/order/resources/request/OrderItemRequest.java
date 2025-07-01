package com.order.resources.request;

import com.order.entity.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(
    @NotBlank(message = "Product ID é obrigatório")
    String productId,

    @NotBlank(message = "Product name é obrigatório")
    String productName,

    @NotNull(message = "Quantity é obrigatório")
    @Positive(message = "Quantity deve ser maior que zero")
    Integer quantity,

    @NotNull(message = "Unit price é obrigatório")
    @Positive(message = "Unit price deve ser maior que zero")
    BigDecimal unitPrice
) {
    public OrderItem toOrderItem(){
        return new OrderItem(productId, productName, quantity, unitPrice);
    }

}