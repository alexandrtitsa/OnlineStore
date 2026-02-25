package com.onlinestore.application.order;

import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderRepository;

import java.util.List;
import java.util.UUID;

public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<Order> getByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }
}