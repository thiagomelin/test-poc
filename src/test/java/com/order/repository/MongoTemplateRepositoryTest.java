package com.order.repository;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MongoTemplateRepositoryTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private MongoTemplateRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchOrders_shouldQueryWithStatusAndExternalId() {
        // Arrange
        OrderStatus status = OrderStatus.SUCCESS;
        String externalId = "ORD123";
        var pageable = PageRequest.of(0, 10);

        List<Order> mockOrders = List.of(new Order());
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(mockOrders);
        when(mongoTemplate.count(any(Query.class), eq(Order.class))).thenReturn(1L);

        // Act
        Page<Order> page = repository.searchOrders(status, externalId, pageable);

        // Assert
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);

        // Capture query to assert criteria
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));
        Query executedQuery = queryCaptor.getValue();

        // Obtem o Document da query
        Document queryObject = executedQuery.getQueryObject();

        List<?> andList = (List<?>) queryObject.get("$and");

        boolean containsExternalId = andList.stream()
                .filter(obj -> obj instanceof Document)
                .map(obj -> (Document) obj)
                .anyMatch(doc -> doc.containsKey("externalId"));

        assertThat(containsExternalId).isTrue();
    }

    @Test
    void searchOrders_shouldQueryWithoutCriteria() {
        // Arrange
        var pageable = PageRequest.of(0, 5);

        List<Order> mockOrders = List.of(new Order(), new Order());
        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(mockOrders);
        when(mongoTemplate.count(any(Query.class), eq(Order.class))).thenReturn(2L);

        // Act
        Page<Order> page = repository.searchOrders(null, null, pageable);

        // Assert
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));

        Query executedQuery = queryCaptor.getValue();
        assertThat(executedQuery.getQueryObject().isEmpty()).isTrue();
    }

    @Test
    void searchOrders_shouldQueryWithOnlyStatus() {
        // Arrange
        var pageable = PageRequest.of(1, 20);

        when(mongoTemplate.find(any(Query.class), eq(Order.class))).thenReturn(List.of());
        when(mongoTemplate.count(any(Query.class), eq(Order.class))).thenReturn(0L);

        // Act
        Page<Order> page = repository.searchOrders(OrderStatus.SUCCESS, null, pageable);

        // Assert
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Order.class));

        Query executedQuery = queryCaptor.getValue();
        var queryObject = executedQuery.getQueryObject();
        assertThat(queryObject.containsKey("status"));
    }
}
