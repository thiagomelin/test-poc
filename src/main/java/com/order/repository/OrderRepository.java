package com.order.repository;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    boolean existsByExternalId(String externalId);

    @Query(value = "{'external_id': ?0}", fields = "{'items': 1, 'externalId': 1, 'customerId': 1, 'totalAmount': 1, 'status': 1, 'processedAt': 1, 'createdAt': 1, 'updatedAt': 1}")
    Optional<Order> findByIdWithItems(String orderId);


}