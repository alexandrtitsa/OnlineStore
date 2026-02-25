package com.onlinestore.domain.exception;

import com.onlinestore.domain.order.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

    private final OrderStatus currentStatus;
    private final OrderStatus attemptedStatus;

    public InvalidOrderStateException(OrderStatus current, OrderStatus attempted) {
        super(String.format("Cannot transition from %s to %s", current, attempted));
        this.currentStatus = current;
        this.attemptedStatus = attempted;
    }

    public InvalidOrderStateException(OrderStatus current, OrderStatus attempted, String reason) {
        super(String.format("Cannot transition from %s to %s: %s", current, attempted, reason));
        this.currentStatus = current;
        this.attemptedStatus = attempted;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}