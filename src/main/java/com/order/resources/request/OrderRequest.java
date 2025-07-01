package com.order.resources.request;

import com.order.entity.Order;
import com.order.entity.OrderItem;
import com.order.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.stream.Collectors;

public record OrderRequest(
    @NotBlank(message = "External ID é obrigatório")
    String externalId,

    @NotBlank(message = "Customer ID é obrigatório")
    String customerId,

    @NotEmpty(message = "Lista de itens não pode estar vazia")
    @Valid
    List<OrderItemRequest> items
) {

    public Order toOrder(){
        List<OrderItem> items = items().stream()
                .map(OrderItemRequest::toOrderItem)
                .collect(Collectors.toList());

        return new Order(externalId, customerId, null, items);
    }

}