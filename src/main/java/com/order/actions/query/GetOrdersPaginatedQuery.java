package com.order.actions.query;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.repository.MongoTemplateRepository;
import com.order.repository.OrderRepository;
import com.order.resources.request.OrderSearchRequest;
import com.order.resources.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetOrdersPaginatedQuery {

    private final MongoTemplateRepository mongoTemplateRepository;

    public Page<OrderResponse> execute(OrderSearchRequest request, Pageable pageable) {
        Page<Order> pagedList = mongoTemplateRepository.searchOrders(request.status(), request.externalId(), pageable);
        List<OrderResponse> response = pagedList.getContent()
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
        return new PageImpl<>(response, pagedList.getPageable(), pagedList.getTotalElements());
    }
}