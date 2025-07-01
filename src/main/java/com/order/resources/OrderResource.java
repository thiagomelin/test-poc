package com.order.resources;

import com.order.actions.command.CreateOrderCommand;
import com.order.actions.query.GetOrderByIdQuery;
import com.order.actions.query.GetOrdersPaginatedQuery;
import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exceptions.DuplicateOrderException;
import com.order.exceptions.OrderNotFoundException;
import com.order.resources.request.OrderRequest;
import com.order.resources.request.OrderSearchRequest;
import com.order.resources.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderResource {

    private final CreateOrderCommand orderService;
    private final GetOrderByIdQuery getOrderById;
    private final GetOrdersPaginatedQuery getOrdersPaginatedQuery;

    @PostMapping
    public ResponseEntity<OrderResponse> receiveOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Recebendo pedido via REST: {}", request.externalId());
        
        try {
            OrderResponse response = orderService.receiveOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicateOrderException e) {
            log.warn("Pedido duplicado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Erro ao processar pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        try {
            Order order = getOrderById.execute(orderId);
            return ResponseEntity.ok(OrderResponse.fromOrder(order));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao buscar pedido {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(@RequestParam(required = false) OrderStatus status,
                                                  @RequestParam(required = false) String externalId,
                                                  Pageable pageable) {
        try {
            OrderSearchRequest request = new OrderSearchRequest(status, externalId);
            return ResponseEntity.ok(getOrdersPaginatedQuery.execute(request, pageable));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao buscar pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }

}