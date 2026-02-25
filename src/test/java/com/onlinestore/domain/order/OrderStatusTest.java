package com.onlinestore.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({
            "DRAFT, PENDING_PAYMENT, true",
            "DRAFT, CANCELLED, true",
            "DRAFT, PAID, false",
            "PENDING_PAYMENT, PAID, true",
            "PENDING_PAYMENT, CANCELLED, true",
            "PENDING_PAYMENT, DRAFT, false",
            "PAID, COMPLETED, true",
            "PAID, CANCELLED, true",
            "PAID, DRAFT, false",
            "COMPLETED, CANCELLED, false",
            "COMPLETED, DRAFT, false",
            "CANCELLED, DRAFT, false",
            "CANCELLED, PENDING_PAYMENT, false"
    })
    void shouldValidateTransitions(OrderStatus from, OrderStatus to, boolean expected) {
        // When
        boolean canTransition = from.canTransitionTo(to);

        // Then
        assertThat(canTransition).isEqualTo(expected);
    }
}