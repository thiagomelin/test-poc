package com.order.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Field("product_id")
    private String productId;

    @Field("product_name")
    private String productName;

    @Field("quantity")
    private Integer quantity;

    @Field("unit_price")
    private BigDecimal unitPrice;

    @Field("subtotal")
    private BigDecimal subtotal;

    public OrderItem(String productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
} 