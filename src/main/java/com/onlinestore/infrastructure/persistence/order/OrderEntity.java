package com.onlinestore.infrastructure.persistence.order;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root for Order.
 *
 * Manages OrderItems as dependent entities with cascade and orphan removal.
 * Uses optimistic locking (@Version) and eager fetch of items to ensure aggregate consistency.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    /**
     * Items in the aggregate.
     * Cascade ALL and orphanRemoval ensure changes propagate correctly.
     * Eager fetch to maintain aggregate consistency.
     */
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER,
            mappedBy = "order"
    )
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatusEntity status;

    @Column
    private String paymentId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Optimistic locking version.
     * Entire aggregate is locked during modification.
     */
    @Version
    private Long version;

    // ==================== AGGREGATE OPERATIONS ====================

    /**
     * Replaces all items in aggregate.
     * Old items are removed (orphanRemoval), new items are added.
     *
     * This ensures aggregate consistency when saving.
     */
    public void setItems(List<OrderItemEntity> newItems) {
        // Clear existing items (triggers orphanRemoval)
        this.items.clear();

        // Add new items and establish bidirectional relationship
        if (newItems != null) {
            newItems.forEach(this::addItem);
        }
    }

    /**
     * Adds item to aggregate and establishes bidirectional relationship.
     */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Removes item from aggregate (triggers orphanRemoval).
     */
    public void removeItem(OrderItemEntity item) {
        items.remove(item);
        item.setOrder(null);
    }

    // ==================== LIFECYCLE CALLBACKS ====================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ==================== GETTERS / SETTERS ====================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public OrderStatusEntity getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEntity status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}