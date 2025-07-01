package com.order.actions.command;

import com.order.exceptions.DuplicateOrderException;
import com.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

public class ValidateIfOrderIsDuplicatedTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ValidateIfOrderIsDuplicated validateIfOrderIsDuplicated;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldThrowException_whenOrderExists() {
        String externalId = "ORDER_123";
        when(orderRepository.existsByExternalId(externalId)).thenReturn(true);

        DuplicateOrderException exception = assertThrows(DuplicateOrderException.class, () ->
                validateIfOrderIsDuplicated.execute(externalId)
        );

        assertEquals("Order has already been processed: ORDER_123", exception.getMessage());
    }

    @Test
    void execute_shouldDoNothing_whenOrderDoesNotExist() {
        String externalId = "ORDER_456";
        when(orderRepository.existsByExternalId(externalId)).thenReturn(false);

        assertDoesNotThrow(() -> validateIfOrderIsDuplicated.execute(externalId));
    }
}
