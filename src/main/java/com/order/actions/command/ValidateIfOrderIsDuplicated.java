package com.order.actions.command;

import com.order.exceptions.DuplicateOrderException;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateIfOrderIsDuplicated {

    private final OrderRepository orderRepository;

    public void execute(String externalId) {
        if (orderRepository.existsByExternalId(externalId)) {
            log.error("[ValidateIfOrderIsDuplicated] error.order.duplicated: {}", externalId);
            throw new DuplicateOrderException("Order has already been processed: " + externalId);
        }
    }

}
