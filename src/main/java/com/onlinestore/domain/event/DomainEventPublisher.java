package com.onlinestore.domain.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}