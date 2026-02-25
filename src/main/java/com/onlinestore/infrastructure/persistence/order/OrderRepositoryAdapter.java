package com.onlinestore.infrastructure.persistence.order;

import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing OrderRepository using JPA (infrastructure layer).
 * Maps between Order domain model and OrderEntity persistence model.
 * Handles aggregate validation and persistence operations.
 */

@Repository
@Transactional(readOnly = true)
class OrderRepositoryAdapter implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryAdapter.class);

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper; // ← Renamed

    OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        log.debug("Saving Order aggregate: id={}, status={}, itemCount={}",
                order.getId(), order.getStatus(), order.getItems().size());

        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        Order result = mapper.toDomain(saved);

        log.debug("Order saved: id={}, version={}", saved.getId(), saved.getVersion());

        return result;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        log.debug("Finding Order: id={}", id);

        return jpaRepository.findByIdWithItems(id)
                .map(entity -> {
                    Order order = mapper.toDomain(entity);
                    order.validateInvariants();
                    log.debug("Order found: id={}, itemCount={}", id, order.getItems().size());
                    return order;
                });
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        log.debug("Finding Orders for user: userId={}", userId);

        List<Order> orders = jpaRepository.findByUserIdWithItems(userId).stream()
                .map(entity -> {
                    Order order = mapper.toDomain(entity);
                    order.validateInvariants();
                    return order;
                })
                .toList();

        log.debug("Found {} orders for user: userId={}", orders.size(), userId);

        return orders;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting Order: id={}", id);
        jpaRepository.deleteById(id);
        log.debug("Order deleted: id={}", id);
    }
}