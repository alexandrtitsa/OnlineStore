package com.onlinestore.domain.order;

import com.onlinestore.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPolicyTest {

    private OrderPolicy orderPolicy;

    @BeforeEach
    void setup() {
        orderPolicy = new DefaultOrderPolicy();
    }

    @Test
    void shouldAllowConfirmationFromDraft() {
        // Given
        Order order = createOrderInStatus(OrderStatus.DRAFT);

        // When / Then
        assertThatNoException().isThrownBy(() -> orderPolicy.validateConfirmation(order));
    }

    @Test
    void shouldRejectConfirmationFromNonDraftStatus() {
        // Given
        Order order = createOrderInStatus(OrderStatus.PENDING_PAYMENT);

        // When / Then
        assertThatThrownBy(() -> orderPolicy.validateConfirmation(order))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void shouldAllowPaymentFromPendingPayment() {
        // Given
        Order order = createOrderInStatus(OrderStatus.PENDING_PAYMENT);

        // When / Then
        assertThatNoException().isThrownBy(() -> orderPolicy.validatePayment(order));
    }

    @Test
    void shouldRejectPaymentFromDraft() {
        // Given
        Order order = createOrderInStatus(OrderStatus.DRAFT);

        // When / Then
        assertThatThrownBy(() -> orderPolicy.validatePayment(order))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldAllowCancellationFromDraft() {
// Given
        Order order = createOrderInStatus(OrderStatus.DRAFT);
        // When / Then
        assertThatNoException().isThrownBy(() -> orderPolicy.validateCancellation(order));
    }

    @Test
    void shouldRejectCancellationFromCompleted() {
        // Given
        Order order = createOrderInStatus(OrderStatus.COMPLETED);

        // When / Then
        assertThatThrownBy(() -> orderPolicy.validateCancellation(order))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldAllowCompletionFromPaid() {
        // Given
        Order order = createOrderInStatus(OrderStatus.PAID);

        // When / Then
        assertThatNoException().isThrownBy(() -> orderPolicy.validateCompletion(order));
    }

    @Test
    void shouldRejectCompletionFromPendingPayment() {
        // Given
        Order order = createOrderInStatus(OrderStatus.PENDING_PAYMENT);

        // When / Then
        assertThatThrownBy(() -> orderPolicy.validateCompletion(order))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    private Order createOrderInStatus(OrderStatus status) {
        var items = List.of(
                new OrderItem(UUID.randomUUID(), "Test Product", 1, BigDecimal.TEN)
        );

        return Order.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                items,
                status,
                null,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }
}