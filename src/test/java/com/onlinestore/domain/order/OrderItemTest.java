package com.onlinestore.domain.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderItemTest {

    @Test
    void shouldCalculateSubtotalCorrectly() {
        // Given
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                "Test Product",
                3,
                BigDecimal.valueOf(10.50)
        );

        // When
        BigDecimal subtotal = item.getSubtotal();

        // Then
        assertThat(subtotal).isEqualByComparingTo("31.50");
    }

    @Test
    void shouldRequirePositiveQuantity() {
        // When / Then
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Product",
                0,
                BigDecimal.TEN
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");

        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Product",
                -1,
                BigDecimal.TEN
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRequireNonNegativePrice() {
        // When / Then
        assertThatThrownBy(() -> new OrderItem(
                UUID.randomUUID(),
                "Product",
                1,
                BigDecimal.valueOf(-10)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be non-negative");
    }
}