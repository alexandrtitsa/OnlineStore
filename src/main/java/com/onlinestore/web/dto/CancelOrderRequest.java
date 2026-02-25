package com.onlinestore.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelOrderRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotBlank(message = "Cancellation reason is required")
        String reason
) {}