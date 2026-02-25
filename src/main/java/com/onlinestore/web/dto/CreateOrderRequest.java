package com.onlinestore.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "Product ID is required")
            UUID productId,

            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity
    ) {}
}