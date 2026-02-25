package com.onlinestore.application.exception;

/**
 * Thrown when an order operation fails due to business rules.
 * Wraps domain exceptions for application layer.
 */
public class OrderOperationException extends ApplicationException {

    public OrderOperationException(String message) {
        super(message);
    }

    public OrderOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}