package com.order.repository;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoTemplateRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<Order> searchOrders(OrderStatus status, String externalId, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (externalId != null) {
            criteriaList.add(Criteria.where("externalId").regex(externalId, "i"));
        }

        Criteria criteria = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList);

        Query query = new Query(criteria).with(pageable);

        List<Order> orders = mongoTemplate.find(query, Order.class);
        long count = mongoTemplate.count(query.skip(0).limit(0), Order.class);

        return new PageImpl<>(orders, pageable, count);
    }

}
