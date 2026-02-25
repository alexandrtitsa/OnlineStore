package com.onlinestore.web.dto.mapper;

import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderItem;
import com.onlinestore.web.dto.OrderItemResponse;
import com.onlinestore.web.dto.OrderResponse;
import org.springframework.stereotype.Component;

/**
 * Maps Order domain objects to DTOs for API responses.
 */
@Component
public class OrderDtoMapper {

    /**
     * Converts domain Order to API response DTO.
     */
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getStatus(),
                order.getPaymentId().orElse(null),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPricePerUnit(),
                item.getSubtotal()
        );
    }
}