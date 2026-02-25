package com.onlinestore.domain.order;

import com.onlinestore.domain.exception.InvalidOrderStateException;

/**
 * Default implementation of business rules for Order state transitions.
 * This is a stateless domain service with pure business logic.
 */
public class DefaultOrderPolicy implements OrderPolicy {

    @Override
    public void validateConfirmation(Order order) {
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new InvalidOrderStateException(
                    order.getStatus(),
                    OrderStatus.PENDING_PAYMENT,
                    "Order can only be confirmed from DRAFT status"
            );
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot confirm order without items");
        }
    }

    @Override
    public void validatePayment(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateException(
                    order.getStatus(),
                    OrderStatus.PAID,
                    "Order can only be paid from PENDING_PAYMENT status"
            );
        }
    }

    @Override
    public void validateCancellation(Order order) {
        OrderStatus currentStatus = order.getStatus();

        // Cannot cancel completed or already cancelled orders
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    currentStatus,
                    OrderStatus.CANCELLED,
                    "Cannot cancel order in " + currentStatus + " status"
            );
        }
    }

    @Override
    public void validateCompletion(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                    order.getStatus(),
                    OrderStatus.COMPLETED,
                    "Order can only be completed from PAID status"
            );
        }
    }
}