package com.onlinestore.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID eventId,
        UUID orderId,
        String paymentId,
        Instant occurredAt
) implements DomainEvent {

    public static PaymentCompletedEvent now(UUID orderId, String paymentId) {
        return new PaymentCompletedEvent(UUID.randomUUID(), orderId, paymentId, Instant.now());
    }
}