package com.onlinestore.application.exception;

import java.util.UUID;

/**
 * Thrown when requested order is not found.
 * This is an application-level exception that can be mapped to HTTP 404.
 */
public class OrderNotFoundException extends ApplicationException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}