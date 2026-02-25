package com.onlinestore.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PayOrderRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotBlank(message = "Payment ID is required")
        String paymentId
) {}