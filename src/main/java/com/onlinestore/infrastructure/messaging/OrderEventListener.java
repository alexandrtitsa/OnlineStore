package com.onlinestore.infrastructure.messaging;

import com.onlinestore.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to domain events published after transaction commit.
 *
 * Uses @TransactionalEventListener to ensure events are processed
 * only after successful commit.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created (after commit): orderId={}", event.orderId());

        // Here you can:
        // - Send notification email
        // - Update analytics
        // - Trigger external systems
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Payment completed (after commit): orderId={}, paymentId={}",
                event.orderId(), event.paymentId());

        // Here you can:
        // - Send payment confirmation email
        // - Update inventory system
        // - Trigger shipping workflow
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Order cancelled (after commit): orderId={}, reason={}",
                event.orderId(), event.reason());

        // Here you can:
        // - Send cancellation email
        // - Refund payment
        // - Update analytics
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment failed (after commit): orderId={}, reason={}",
                event.orderId(), event.reason());

        // Here you can:
        // - Send payment failure notification
        // - Retry payment
        // - Cancel order automatically
    }
}