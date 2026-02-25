package com.onlinestore.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for OrderEntity.
 * Used internally by OrderRepository (domain port).
 */
interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Finds all orders for a specific user with items eagerly loaded to avoid N+1 problem.
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.userId = :userId")
    List<OrderEntity> findByUserIdWithItems(@Param("userId") UUID userId);

    /**
     * Finds an order by ID with items eagerly loaded.
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);
}