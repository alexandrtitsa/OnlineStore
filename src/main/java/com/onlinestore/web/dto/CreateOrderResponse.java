package com.onlinestore.web.dto;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        String message
) {
    public static CreateOrderResponse success(UUID orderId) {
        return new CreateOrderResponse(orderId, "Order created successfully");
    }
}