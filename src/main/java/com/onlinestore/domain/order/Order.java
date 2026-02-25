package com.onlinestore.domain.order;

import com.onlinestore.domain.event.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate root representing an Order.
 *
 * Owns OrderItem entities and enforces aggregate invariants.
 * Publishes domain events on state transitions.
 */
public class Order {

    private final UUID id;
    private final UUID userId;

    /** Items owned by this aggregate (cannot exist independently). */
    private final List<OrderItem> items;

    private OrderStatus status;
    private String paymentId;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ==================== FACTORY METHOD ====================

    /**
     * Creates new Order aggregate.
     * Creates Order in DRAFT state with provided items.
     */
    public static Order create(UUID userId, List<OrderItem> items) {
        validateItems(items);

        Order order = new Order(UUID.randomUUID(), userId, new ArrayList<>(items));
        order.domainEvents.add(OrderCreatedEvent.now(order.id));
        return order;
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    // ==================== CONSTRUCTOR ====================

    private Order(UUID id, UUID userId, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = OrderStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Reconstitutes aggregate from persistence state.
     * Used by repository to restore full aggregate.
     */
    public static Order reconstitute(
            UUID id,
            UUID userId,
            List<OrderItem> items,
            OrderStatus status,
            String paymentId,
            Instant createdAt,
            Instant updatedAt
    ) {
        validateItems(items); // Ensure aggregate invariants

        Order order = new Order(id, userId, new ArrayList<>(items));
        order.status = status;
        order.paymentId = paymentId;
        return order;
    }

    // ==================== BEHAVIOR ====================

    public void markAsPendingPayment() {
        changeStatus(OrderStatus.PENDING_PAYMENT);
    }

    public void markAsPaid(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }

        this.paymentId = paymentId;
        changeStatus(OrderStatus.PAID);
        this.domainEvents.add(PaymentCompletedEvent.now(this.id, paymentId));
    }

    public void markPaymentFailed(String reason) {
        this.domainEvents.add(PaymentFailedEvent.now(this.id, reason));
    }

    public void markAsCancelled(String reason) {
        changeStatus(OrderStatus.CANCELLED);
        this.domainEvents.add(OrderCancelledEvent.now(this.id, reason));
    }

    public void markAsCompleted() {
        changeStatus(OrderStatus.COMPLETED);
    }

    private void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    // ==================== CALCULATIONS ====================
    /**
     * Calculates total amount (derived value).
     */
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== QUERIES ====================

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED;
    }

    // ==================== GETTERS ====================

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    /**
     * Returns unmodifiable view of items.
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Optional<String> getPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ==================== INVARIANT VALIDATION ====================

    /**
     * Validates aggregate invariants.
     * Called after reconstitution.
     */
    public void validateInvariants() {
        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "Order invariant violated: must have at least one item"
            );
        }
    }
}
