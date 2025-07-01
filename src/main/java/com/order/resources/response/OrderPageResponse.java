package com.order.resources.response;

import com.order.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPageResponse {
    
    private List<OrderResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static OrderPageResponse fromPage(Page<Order> page) {
        List<OrderResponse> orderResponses = page.getContent()
                .stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
        
        return OrderPageResponse.builder()
                .content(orderResponses)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
} 