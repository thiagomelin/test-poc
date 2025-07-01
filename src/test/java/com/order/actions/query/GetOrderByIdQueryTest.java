package com.order.actions.query;

import com.order.entity.Order;
import com.order.exceptions.OrderNotFoundException;
import com.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetOrderByIdQueryTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GetOrderFromRedisQuery getOrderFromCache;

    @InjectMocks
    private GetOrderByIdQuery getOrderByIdQuery;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldReturnOrderFromCache_whenOrderIsCached() {
        String orderId = "ORDER_1";
        Order cachedOrder = new Order();
        when(getOrderFromCache.execute(orderId)).thenReturn(cachedOrder);

        Order result = getOrderByIdQuery.execute(orderId);

        assertSame(cachedOrder, result);
        verify(getOrderFromCache).execute(orderId);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void execute_shouldReturnOrderFromDatabase_whenOrderIsNotCached() {
        String orderId = "ORDER_2";
        when(getOrderFromCache.execute(orderId)).thenReturn(null);
        Order dbOrder = new Order();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(dbOrder));

        Order result = getOrderByIdQuery.execute(orderId);

        assertSame(dbOrder, result);
        verify(getOrderFromCache).execute(orderId);
        verify(orderRepository).findByIdWithItems(orderId);
    }

    @Test
    void execute_shouldThrowException_whenOrderNotFoundInCacheAndDatabase() {
        String orderId = "ORDER_3";
        when(getOrderFromCache.execute(orderId)).thenReturn(null);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> getOrderByIdQuery.execute(orderId)
        );

        assertEquals("order.not.found", exception.getMessage());
        verify(getOrderFromCache).execute(orderId);
        verify(orderRepository).findByIdWithItems(orderId);
    }
}
