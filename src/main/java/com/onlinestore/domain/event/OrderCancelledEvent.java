package com.onlinestore.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public static OrderCancelledEvent now(UUID orderId, String reason) {
        return new OrderCancelledEvent(UUID.randomUUID(), orderId, reason, Instant.now());
    }
}