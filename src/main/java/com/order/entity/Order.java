package com.order.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@CompoundIndexes({
    @CompoundIndex(name = "idx_status_created_at", def = "{'status': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "idx_customer_created_at", def = "{'customerId': 1, 'createdAt': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("external_id")
    private String externalId;

    @Indexed
    @Field("customer_id")
    private String customerId;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Indexed
    @Field("status")
    private OrderStatus status;

    @Field("processed_at")
    private LocalDateTime processedAt;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("items")
    private List<OrderItem> items;


    public Order(String externalId, String customerId, OrderStatus status, List<OrderItem> items) {
        this.externalId = externalId;
        this.customerId = customerId;
        this.status = status;
        this.items = items;
        calculateTotal();
    }

    public void calculateTotal() {
        if (items != null && !items.isEmpty()) {
            this.totalAmount = items.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

}