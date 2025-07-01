package com.order.resources.request;

import com.order.entity.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record OrderSearchRequest(
        OrderStatus status,
        String externalId
) {
    

}