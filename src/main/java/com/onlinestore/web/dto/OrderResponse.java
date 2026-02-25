package com.onlinestore.web.dto;

import com.onlinestore.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order details response")
public record OrderResponse(
        @Schema(description = "Order identifier")
        UUID id,

        @Schema(description = "User identifier")
        UUID userId,

        @Schema(description = "Order items")
        List<OrderItemResponse> items,

        @Schema(description = "Order status")
        OrderStatus status,

        @Schema(description = "Payment identifier", nullable = true)
        String paymentId,

        @Schema(description = "Total order amount")
        BigDecimal totalAmount,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
}