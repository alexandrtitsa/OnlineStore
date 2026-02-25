package com.onlinestore.infrastructure.messaging;

import com.onlinestore.domain.event.DomainEvent;
import com.onlinestore.domain.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes domain events via Spring's event mechanism.
 *
 * CRITICAL: Events are published AFTER transaction commit.
 * This prevents phantom events if transaction rolls back.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publishes event AFTER transaction commit.
     *
     * If called within transaction:
     * - Event is queued and published after commit
     * - If transaction rolls back, event is NOT published
     *
     * If called outside transaction:
     * - Event is published immediately
     */
    @Override
    public void publish(DomainEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // We're in a transaction - publish AFTER commit
            log.debug("Queuing event for after-commit publication: {}",
                    event.getClass().getSimpleName());

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.info("Publishing event after commit: {} for order: {}",
                                    event.getClass().getSimpleName(), event.orderId());
                            applicationEventPublisher.publishEvent(event);
                        }

                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                log.warn("Transaction rolled back - event NOT published: {}",
                                        event.getClass().getSimpleName());
                            }
                        }
                    }
            );
        } else {
            // No transaction - publish immediately
            log.info("Publishing event immediately (no transaction): {}",
                    event.getClass().getSimpleName());
            applicationEventPublisher.publishEvent(event);
        }
    }
}