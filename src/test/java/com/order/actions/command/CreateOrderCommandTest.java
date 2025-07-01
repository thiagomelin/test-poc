package com.order.actions.command;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exceptions.DuplicateOrderException;
import com.order.repository.OrderRepository;
import com.order.resources.request.OrderRequest;
import com.order.resources.response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateOrderCommandTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddOrderToCacheCommand addOrderToCacheCommand;

    @Mock
    private SaveRedisLockCommand saveRedisLockCommand;

    @Mock
    private ValidateIfOrderIsDuplicated validateIfOrderIsDuplicated;

    @InjectMocks
    private CreateOrderCommand createOrderCommand;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void receiveOrder_shouldCreateOrderSuccessfully() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.externalId()).thenReturn("ORDER_123");

        Order order = new Order();
        order.setExternalId("ORDER_123");

        when(request.toOrder()).thenReturn(order);

        Order savedOrder = new Order();
        savedOrder.setExternalId("ORDER_123");

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = createOrderCommand.receiveOrder(request);

        assertNotNull(response);
        verify(saveRedisLockCommand).execute("ORDER_123");
        verify(validateIfOrderIsDuplicated).execute("ORDER_123");
        verify(orderRepository, times(1)).save(order);
        verify(addOrderToCacheCommand).execute(order);
        verify(saveRedisLockCommand).releaseLock("ORDER_123");

        assertEquals(OrderStatus.SUCCESS, order.getStatus());
    }

    @Test
    void receiveOrder_shouldHandleDuplicateOrderException() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.externalId()).thenReturn("ORDER_DUP");

        Order order = new Order();
        order.setExternalId("ORDER_DUP");

        when(request.toOrder()).thenReturn(order);

        doThrow(new DuplicateOrderException("Duplicate order"))
                .when(validateIfOrderIsDuplicated).execute("ORDER_DUP");

        OrderResponse response = createOrderCommand.receiveOrder(request);

        assertNull(response);
        verify(saveRedisLockCommand).execute("ORDER_DUP");
        verify(validateIfOrderIsDuplicated).execute("ORDER_DUP");
        verify(orderRepository, never()).save(any());
        verify(addOrderToCacheCommand, never()).execute(any());
        verify(saveRedisLockCommand).releaseLock("ORDER_DUP");
    }

    @Test
    void receiveOrder_shouldHandleGenericException() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.externalId()).thenReturn("ORDER_ERR");

        Order order = new Order();
        order.setExternalId("ORDER_ERR");

        when(request.toOrder()).thenReturn(order);

        doThrow(new RuntimeException("Unexpected error"))
                .when(validateIfOrderIsDuplicated).execute("ORDER_ERR");

        OrderResponse response = createOrderCommand.receiveOrder(request);

        assertNull(response);
        verify(saveRedisLockCommand).execute("ORDER_ERR");
        verify(validateIfOrderIsDuplicated).execute("ORDER_ERR");
        verify(orderRepository).save(order);
        verify(addOrderToCacheCommand, never()).execute(any());
        verify(saveRedisLockCommand).releaseLock("ORDER_ERR");

        assertEquals(OrderStatus.FAILED, order.getStatus());
    }
}
