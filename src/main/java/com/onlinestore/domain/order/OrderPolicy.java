package com.onlinestore.domain.order;

/**
 * Domain service for order validation rules (pure domain logic).
 */
public interface OrderPolicy {

    /** Validate confirmation (DRAFT → PENDING_PAYMENT) */
    void validateConfirmation(Order order);

    /** Validate payment (PENDING_PAYMENT → PAID) */
    void validatePayment(Order order);

    /** Validate cancellation */
    void validateCancellation(Order order);

    /** Validate completion (PAID → COMPLETED) */
    void validateCompletion(Order order);
}
