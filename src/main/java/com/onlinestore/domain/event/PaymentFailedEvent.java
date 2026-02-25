package com.onlinestore.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public static PaymentFailedEvent now(UUID orderId, String reason) {
        return new PaymentFailedEvent(UUID.randomUUID(), orderId, reason, Instant.now());
    }
}