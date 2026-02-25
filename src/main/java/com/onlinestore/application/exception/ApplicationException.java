package com.onlinestore.application.exception;

/**
 * Base exception for application layer.
 * Wraps domain exceptions and adds application-specific context.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}