package com.onlinestore.domain.order;

public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    COMPLETED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == PENDING_PAYMENT || newStatus == CANCELLED;
            case PENDING_PAYMENT -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == COMPLETED || newStatus == CANCELLED;
            case CANCELLED, COMPLETED -> false; // термінальні стани
        };
    }
}