package com.onlinestore.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static OrderCreatedEvent now(UUID orderId) {
        return new OrderCreatedEvent(UUID.randomUUID(), orderId, Instant.now());
    }
}