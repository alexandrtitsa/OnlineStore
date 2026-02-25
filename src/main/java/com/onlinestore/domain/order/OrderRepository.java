package com.onlinestore.domain.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order Aggregate (Domain Port).
 * Implementation details are in infrastructure layer.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findByUserId(UUID userId);

    void delete(UUID id);

    default boolean exists(UUID id) {
        return findById(id).isPresent();
    }
}