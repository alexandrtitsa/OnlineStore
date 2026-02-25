package com.onlinestore.domain.order;

import com.onlinestore.domain.event.OrderCreatedEvent;
import com.onlinestore.domain.event.PaymentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private OrderPolicy orderPolicy;

    @BeforeEach
    void setup() {
        orderPolicy = new DefaultOrderPolicy();
    }

    @Test
    void shouldCreateOrderInDraftStatus() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product A", 2, BigDecimal.valueOf(10))
        );
        UUID userId = UUID.randomUUID();

        // When
        Order order = Order.create(userId, items);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(OrderCreatedEvent.class);
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Product A", 2, BigDecimal.valueOf(10)),
                new OrderItem(UUID.randomUUID(), "Product B", 1, BigDecimal.valueOf(5)),
                new OrderItem(UUID.randomUUID(), "Product C", 3, BigDecimal.valueOf(7.50))
        );

        // When
        var order = Order.create(UUID.randomUUID(), items);

        // Then
        assertThat(order.getTotalAmount()).isEqualByComparingTo("47.50");
    }

    @Test
    void shouldTransitionFromDraftToPendingPayment() {
        // Given
        var order = createTestOrder();

        // When
        orderPolicy.validateConfirmation(order);
        order.markAsPendingPayment();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void shouldTransitionFromPendingPaymentToPaid() {
        // Given
        var order = createTestOrder();
        orderPolicy.validateConfirmation(order);
        order.markAsPendingPayment();

        // When
        orderPolicy.validatePayment(order);
        order.markAsPaid("PAYMENT-123");

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentId()).hasValue("PAYMENT-123");
        assertThat(order.getDomainEvents())
                .filteredOn(e -> e instanceof PaymentCompletedEvent)
                .hasSize(1);
    }

    @Test
    void shouldTransitionFromPaidToCompleted() {
        // Given
        var order = createTestOrder();
        orderPolicy.validateConfirmation(order);
        order.markAsPendingPayment();
        orderPolicy.validatePayment(order);
        order.markAsPaid("PAYMENT-123");

        // When
        orderPolicy.validateCompletion(order);
        order.markAsCompleted();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldCancelOrderFromDraft() {
        // Given
        var order = createTestOrder();

        // When
        orderPolicy.validateCancellation(order);
        order.markAsCancelled("User cancelled");

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotTransitionFromCompletedToCancelled() {
        // Given
        var order = createTestOrder();
        orderPolicy.validateConfirmation(order);
        order.markAsPendingPayment();
        orderPolicy.validatePayment(order);
        order.markAsPaid("PAYMENT-123");
        orderPolicy.validateCompletion(order);
        order.markAsCompleted();

        // When / Then
        assertThatThrownBy(() -> orderPolicy.validateCancellation(order))
                .isInstanceOf(com.onlinestore.domain.exception.InvalidOrderStateException.class)
                .hasMessageContaining("Cannot cancel order in COMPLETED status");
    }

    @Test
    void shouldRequireAtLeastOneItem() {
        // When / Then
        assertThatThrownBy(() -> Order.create(UUID.randomUUID(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    private Order createTestOrder() {
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Test Product", 1, BigDecimal.TEN)
        );
        return Order.create(UUID.randomUUID(), items);
    }
}