package com.order.actions.query;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.repository.MongoTemplateRepository;
import com.order.resources.request.OrderSearchRequest;
import com.order.resources.response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetOrdersPaginatedQueryTest {

    @Mock
    private MongoTemplateRepository mongoTemplateRepository;
    @InjectMocks
    private GetOrdersPaginatedQuery getOrdersPaginatedQuery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldReturnMappedPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        OrderSearchRequest searchRequest = new OrderSearchRequest(OrderStatus.SUCCESS, "EXT123");

        Order order = new Order();
        order.setId("order-id-1");
        order.setExternalId("EXT123");
        order.setCustomerId("CUST123");
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setStatus(OrderStatus.SUCCESS);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setProcessedAt(LocalDateTime.now());
        order.setItems(Collections.emptyList());

        Page<Order> mockPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(mongoTemplateRepository.searchOrders(
                eq(OrderStatus.SUCCESS),
                eq("EXT123"),
                eq(pageable))
        ).thenReturn(mockPage);

        // Act
        Page<OrderResponse> result = getOrdersPaginatedQuery.execute(searchRequest, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        OrderResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(order.getId());
        assertThat(response.externalId()).isEqualTo(order.getExternalId());
        assertThat(response.customerId()).isEqualTo(order.getCustomerId());
        assertThat(response.totalAmount()).isEqualTo(order.getTotalAmount());
        assertThat(response.status()).isEqualTo(order.getStatus());

        // Verify the repository interaction
        verify(mongoTemplateRepository, times(1))
                .searchOrders(OrderStatus.SUCCESS, "EXT123", pageable);
    }
}
