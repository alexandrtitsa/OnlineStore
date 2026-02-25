package com.onlinestore.application.order;

import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderPolicy;
import com.onlinestore.domain.order.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for completing an order after delivery.
 * This would typically be triggered by a delivery confirmation system.
 */
public class CompleteOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final OrderPolicy orderPolicy;

    public CompleteOrderUseCase(
            OrderRepository orderRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.orderPolicy = orderPolicy;
    }

    @Transactional
    public void execute(CompleteOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate business rules
        orderPolicy.validateCompletion(order);

        // Complete order
        order.markAsCompleted();

        // Save
        Order savedOrder = orderRepository.save(order);

        // Publish events
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
    }

    public record CompleteOrderCommand(UUID orderId) {}
}