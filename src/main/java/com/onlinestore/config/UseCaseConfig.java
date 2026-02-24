package com.onlinestore.config;

import com.onlinestore.application.order.*;
import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.order.DefaultOrderPolicy;
import com.onlinestore.domain.order.OrderPolicy;
import com.onlinestore.domain.order.OrderRepository;
import com.onlinestore.domain.product.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    // ==================== DOMAIN SERVICES ====================

    @Bean
    public OrderPolicy orderPolicy() {
        return new DefaultOrderPolicy();
    }

    // ==================== USE CASES ====================

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        return new CreateOrderUseCase(
                orderRepository,
                productRepository,
                eventPublisher,
                orderPolicy
        );
    }

    @Bean
    public PayOrderUseCase payOrderUseCase(
            OrderRepository orderRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        return new PayOrderUseCase(orderRepository, eventPublisher, orderPolicy);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        return new CancelOrderUseCase(
                orderRepository,
                productRepository,
                eventPublisher,
                orderPolicy
        );
    }

    @Bean
    public CompleteOrderUseCase completeOrderUseCase(
            OrderRepository orderRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        return new CompleteOrderUseCase(orderRepository, eventPublisher, orderPolicy);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository orderRepository) {
        return new GetOrderUseCase(orderRepository);
    }
}