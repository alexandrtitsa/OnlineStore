package com.onlinestore.infrastructure.persistence.order;

public enum OrderStatusEntity {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    COMPLETED
}