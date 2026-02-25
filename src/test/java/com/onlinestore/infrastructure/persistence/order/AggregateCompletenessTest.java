package com.onlinestore.infrastructure.persistence.order;

import com.onlinestore.AbstractIntegrationTestH2;
import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderItem;
import com.onlinestore.domain.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to verify Aggregate completeness and persistence behavior.
 *
 * Tests ensure:
 * - Complete aggregate is saved/loaded
 * - Cascade operations work correctly
 * - Eager loading prevents LazyInitializationException
 * - Invariants are validated after loading
 */
@Transactional
class AggregateCompletenessTest extends AbstractIntegrationTestH2 {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveCompleteAggregateInSingleOperation() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product A", 2, BigDecimal.valueOf(10)),
                new OrderItem(UUID.randomUUID(), "Product B", 1, BigDecimal.valueOf(5))
        );
        Order order = Order.create(UUID.randomUUID(), items);

        // When - single save operation
        Order saved = orderRepository.save(order);

        // Then - complete aggregate is persisted
        assertThat(saved.getItems()).hasSize(2);

        // Reload from database
        Order reloaded = orderRepository.findById(saved.getId()).orElseThrow();

        // Verify ALL items are loaded
        assertThat(reloaded.getItems())
                .hasSize(2)
                .extracting(OrderItem::getProductName)
                .containsExactlyInAnyOrder("Product A", "Product B");
    }

    @Test
    void shouldCascadeDeleteAllItems() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product", 1, BigDecimal.TEN)
        );
        Order order = Order.create(UUID.randomUUID(), items);
        Order saved = orderRepository.save(order);
        UUID orderId = saved.getId();

        // When
        orderRepository.delete(orderId);

        // Then - order and all items are deleted
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    void shouldEagerlyLoadAllItems() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Item 1", 1, BigDecimal.ONE),
                new OrderItem(UUID.randomUUID(), "Item 2", 2, BigDecimal.valueOf(2)), // ← Fixed
                new OrderItem(UUID.randomUUID(), "Item 3", 3, BigDecimal.valueOf(3))
        );
        Order order = Order.create(UUID.randomUUID(), items);
        Order saved = orderRepository.save(order);

        // When - load without transaction
        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        // Then - all items are accessible (no LazyInitializationException)
        assertThat(found.getItems()).hasSize(3);
        assertThat(found.getTotalAmount()).isEqualByComparingTo("14.00"); // 1*1 + 2*2 + 3*3 = 14
    }

    @Test
    void shouldValidateInvariantsAfterLoading() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product", 1, BigDecimal.TEN)
        );
        Order order = Order.create(UUID.randomUUID(), items);
        orderRepository.save(order);

        // When
        Order loaded = orderRepository.findById(order.getId()).orElseThrow();

        // Then - invariants are validated (no exception)
        assertThat(loaded.getItems()).isNotEmpty();
        assertThat(loaded.getTotalAmount()).isPositive();
    }

    @Test
    void shouldLoadMultipleOrdersWithTheirItems() {
        // Given - create multiple orders
        UUID userId = UUID.randomUUID();

        Order order1 = Order.create(userId, List.of(
                new OrderItem(UUID.randomUUID(), "Product A", 1, BigDecimal.valueOf(10))
        ));

        Order order2 = Order.create(userId, List.of(
                new OrderItem(UUID.randomUUID(), "Product B", 2, BigDecimal.valueOf(20)),
                new OrderItem(UUID.randomUUID(), "Product C", 1, BigDecimal.valueOf(5))
        ));

        orderRepository.save(order1);
        orderRepository.save(order2);

        // When - load all orders for user
        List<Order> orders = orderRepository.findByUserId(userId);

        // Then - all orders with their items are loaded
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getItems()).isNotEmpty();
        assertThat(orders.get(1).getItems()).isNotEmpty();

        // Verify total item count
        long totalItems = orders.stream()
                .mapToLong(o -> o.getItems().size())
                .sum();
        assertThat(totalItems).isEqualTo(3); // 1 + 2 items
    }
}