package com.onlinestore.infrastructure.persistence.order;

import com.onlinestore.AbstractIntegrationTestH2;
import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderItem;
import com.onlinestore.domain.order.OrderRepository;
import com.onlinestore.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OrderRepositoryAdapter.
 * Tests the full persistence layer including mapping.
 */
@Transactional
class OrderRepositoryAdapterTest extends AbstractIntegrationTestH2 {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndFindOrder() {
        // Given
        UUID userId = UUID.randomUUID();
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product A", 2, BigDecimal.valueOf(10)),
                new OrderItem(UUID.randomUUID(), "Product B", 1, BigDecimal.valueOf(5))
        );
        Order order = Order.create(userId, items);
        order.markAsPendingPayment();

        // When
        Order saved = orderRepository.save(order);

        // Then
        assertThat(saved.getId()).isNotNull();

        Optional<Order> found = orderRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void shouldFindOrdersByUserId() {
        // Given
        UUID userId = UUID.randomUUID();

        Order order1 = createTestOrder(userId);
        Order order2 = createTestOrder(userId);
        Order order3 = createTestOrder(UUID.randomUUID());

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        // When
        List<Order> userOrders = orderRepository.findByUserId(userId);

        // Then
        assertThat(userOrders)
                .hasSize(2)
                .extracting(Order::getUserId)
                .containsOnly(userId);
    }

    @Test
    void shouldDeleteOrder() {
        // Given
        Order order = createTestOrder(UUID.randomUUID());
        Order saved = orderRepository.save(order);
        UUID orderId = saved.getId();

        // When
        orderRepository.delete(orderId);

        // Then
        Optional<Order> found = orderRepository.findById(orderId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldPersistOrderItems() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Laptop", 1, BigDecimal.valueOf(999.99)),
                new OrderItem(UUID.randomUUID(), "Mouse", 2, BigDecimal.valueOf(29.99)),
                new OrderItem(UUID.randomUUID(), "Keyboard", 1, BigDecimal.valueOf(79.99))
        );
        Order order = Order.create(UUID.randomUUID(), items);

        // When
        Order saved = orderRepository.save(order);

        // Then
        Order found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getItems()).hasSize(3);
        assertThat(found.getItems())
                .extracting(OrderItem::getProductName)
                .containsExactlyInAnyOrder("Laptop", "Mouse", "Keyboard");
        assertThat(found.getTotalAmount()).isEqualByComparingTo("1139.96");
    }

    private Order createTestOrder(UUID userId) {
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Test Product", 1, BigDecimal.TEN)
        );
        return Order.create(userId, items);
    }
}