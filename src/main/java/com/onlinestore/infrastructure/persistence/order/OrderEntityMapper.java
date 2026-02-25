package com.onlinestore.infrastructure.persistence.order;

import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderItem;
import com.onlinestore.domain.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Maps between Domain model (Order) and Persistence model (OrderEntity).
 *
 * SINGLE RESPONSIBILITY: Domain ↔ Persistence mapping only.
 * Does NOT know about DTOs or web layer.
 */
@Component
class OrderEntityMapper {

    /**
     * Converts domain Order to persistence OrderEntity.
     */
    OrderEntity toEntity(Order domain) {
        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setStatus(toEntityStatus(domain.getStatus()));
        entity.setPaymentId(domain.getPaymentId().orElse(null));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        domain.getItems().forEach(item -> {
            OrderItemEntity itemEntity = toItemEntity(item);
            entity.addItem(itemEntity);
        });

        return entity;
    }

    /**
     * Converts persistence OrderEntity to domain Order.
     * Reconstitutes full aggregate from database state.
     */
    Order toDomain(OrderEntity entity) {
        return Order.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getItems().stream()
                        .map(this::toItemDomain)
                        .collect(Collectors.toList()),
                toDomainStatus(entity.getStatus()),
                entity.getPaymentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private OrderItemEntity toItemEntity(OrderItem domain) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setProductId(domain.getProductId());
        entity.setProductName(domain.getProductName());
        entity.setQuantity(domain.getQuantity());
        entity.setPricePerUnit(domain.getPricePerUnit());
        return entity;
    }

    private OrderItem toItemDomain(OrderItemEntity entity) {
        return new OrderItem(
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                entity.getPricePerUnit()
        );
    }

    private OrderStatusEntity toEntityStatus(OrderStatus domain) {
        return OrderStatusEntity.valueOf(domain.name());
    }

    private OrderStatus toDomainStatus(OrderStatusEntity entity) {
        return OrderStatus.valueOf(entity.name());
    }
}