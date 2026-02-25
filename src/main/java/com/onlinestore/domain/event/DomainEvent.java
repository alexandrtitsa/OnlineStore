package com.onlinestore.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits
        OrderCreatedEvent,
        PaymentCompletedEvent,
        PaymentFailedEvent,
        OrderCancelledEvent {

    UUID eventId();
    UUID orderId();
    Instant occurredAt();
}