package com.onlinestore.application.order;

import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderPolicy;
import com.onlinestore.domain.order.OrderRepository;
import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    private final OrderPolicy orderPolicy;

    public CancelOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.orderPolicy = orderPolicy;
    }

    @Transactional
    public void execute(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 1. Validate business rules
        orderPolicy.validateCancellation(order);

        // 2. Cancel order
        order.markAsCancelled(command.reason());

        // 3. Release stock
        order.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow();
            product.releaseStock(item.getQuantity());
            productRepository.save(product);
        });

        // 4. Save
        Order savedOrder = orderRepository.save(order);

        // 5. Publish events
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
    }

    public record CancelOrderCommand(UUID orderId, String reason) {}
}