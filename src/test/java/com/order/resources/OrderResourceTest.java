package com.order.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.actions.command.CreateOrderCommand;
import com.order.actions.query.GetOrderByIdQuery;
import com.order.actions.query.GetOrdersPaginatedQuery;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import com.order.entity.OrderStatus;
import com.order.exceptions.DuplicateOrderException;
import com.order.exceptions.OrderNotFoundException;
import com.order.resources.request.OrderItemRequest;
import com.order.resources.request.OrderRequest;
import com.order.resources.response.OrderItemResponse;
import com.order.resources.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderResource.class)
class OrderResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderCommand createOrderCommand;
    @MockBean
    private GetOrdersPaginatedQuery getOrdersPaginatedQuery;

    @MockBean
    private GetOrderByIdQuery getOrderByIdQuery;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void receiveOrder_shouldReturn201_whenSuccess() throws Exception {
        OrderRequest request = new OrderRequest("ORDER123", "Customer123", List.of(new OrderItemRequest("product123",
                "Product XYZ", 2, BigDecimal.TEN)));

        OrderResponse response = getOrderResponse();

        when(createOrderCommand.receiveOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("ORDER123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    private static OrderResponse getOrderResponse() {
        OrderItemResponse itemResponse = new OrderItemResponse(
                "ITEM1",
                "Product A",
                2,
                new BigDecimal("50.00"),
                new BigDecimal("100.00")
        );
        return new OrderResponse("123", "ORDER123", "Customer XYZ",
                new BigDecimal("100.00"), OrderStatus.SUCCESS, null, LocalDateTime.now(), LocalDateTime.now(), List.of(itemResponse));
    }

    @Test
    void receiveOrder_shouldReturn409_whenDuplicate() throws Exception {
        OrderRequest request = new OrderRequest("ORDER123", "Customer123", List.of(new OrderItemRequest("product123",
                "Product XYZ", 2, BigDecimal.TEN)));

        when(createOrderCommand.receiveOrder(any(OrderRequest.class)))
                .thenThrow(new DuplicateOrderException("Duplicate order"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void receiveOrder_shouldReturn500_whenUnexpectedError() throws Exception {
        OrderRequest request = new OrderRequest("ORDER123", "Customer123", List.of(new OrderItemRequest("product123",
                "Product XYZ", 2, BigDecimal.TEN)));

        when(createOrderCommand.receiveOrder(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOrderById_shouldReturn200_whenFound() throws Exception {
        String orderId = "ORDER123";
        String externalId = "EXTERNAL123";
        Order order = Order.builder()
                .id(orderId)
                .externalId(externalId)
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.SUCCESS)
                .items(List.of(OrderItem.builder().productName("Product XYZ").quantity(2).build())).build();

        when(getOrderByIdQuery.execute(orderId)).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalAmount").value(BigDecimal.TEN));
    }

    @Test
    void getOrderById_shouldReturn404_whenNotFound() throws Exception {
        String orderId = "ORDER_NOT_FOUND";

        when(getOrderByIdQuery.execute(orderId))
                .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_shouldReturn500_whenUnexpectedError() throws Exception {
        String orderId = "ORDER_ERROR";

        when(getOrderByIdQuery.execute(orderId))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isInternalServerError());
    }

}


