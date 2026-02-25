package com.onlinestore.application.order;

import com.onlinestore.application.exception.OrderNotFoundException;
import com.onlinestore.application.exception.OrderOperationException;
import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.exception.InvalidOrderStateException;
import com.onlinestore.domain.order.Order;
import com.onlinestore.domain.order.OrderPolicy;
import com.onlinestore.domain.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use Case: Pay for Order
 *
 * RESPONSIBILITIES:
 * - Validate order can be paid
 * - Mark order as paid
 * - Persist changes
 * - Publish domain events
 *
 * TRANSACTION BOUNDARY:
 * - Single transaction for entire operation
 * - Events published after commit
 *
 * EXCEPTION HANDLING:
 * - Domain exceptions wrapped in application exceptions
 * - Not found errors mapped to OrderNotFoundException
 */
public class PayOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(PayOrderUseCase.class);

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final OrderPolicy orderPolicy;

    public PayOrderUseCase(
            OrderRepository orderRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.orderPolicy = orderPolicy;
    }

    /**
     * Executes order payment workflow.
     *
     * WORKFLOW:
     * 1. Find order by ID
     * 2. Validate business rules (can be paid)
     * 3. Mark order as paid
     * 4. Save order
     * 5. Publish domain events (after commit)
     *
     * @param command payment command
     * @throws OrderNotFoundException if order not found
     * @throws OrderOperationException if payment fails
     */
    @Transactional
    public void execute(PayOrderCommand command) {
        log.info("Processing payment for order: orderId={}, paymentId={}",
                command.orderId(), command.paymentId());

        // Step 1: Find order (throws OrderNotFoundException if not found)
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        try {
            // Step 2: Validate business rules
            orderPolicy.validatePayment(order);

            // Step 3: Mark as paid
            order.markAsPaid(command.paymentId());

            // Step 4: Save changes
            Order savedOrder = orderRepository.save(order);

            log.info("Order paid successfully: orderId={}, status={}, paymentId={}",
                    savedOrder.getId(), savedOrder.getStatus(), savedOrder.getPaymentId());

            // Step 5: Publish events (after commit)
            savedOrder.getDomainEvents().forEach(event -> {
                log.debug("Queueing event for publication: {}", event.getClass().getSimpleName());
                eventPublisher.publish(event);
            });
            savedOrder.clearDomainEvents();

            // Transaction commits here ↑

        } catch (InvalidOrderStateException e) {
            // Wrap domain exception in application exception
            log.warn("Payment failed - invalid order state: orderId={}, currentStatus={}, message={}",
                    command.orderId(), order.getStatus(), e.getMessage());
            throw new OrderOperationException(
                    "Cannot pay order: " + e.getMessage(), e
            );
        } catch (IllegalArgumentException e) {
            // Handle invalid payment ID or other validation errors
            log.warn("Payment failed - invalid payment data: orderId={}, message={}",
                    command.orderId(), e.getMessage());
            throw new OrderOperationException(
                    "Invalid payment data: " + e.getMessage(), e
            );
        } catch (Exception e) {
            // Unexpected error
            log.error("Payment failed with unexpected error: orderId={}", command.orderId(), e);
            throw new OrderOperationException(
                    "Payment failed due to unexpected error", e
            );
        }
    }

    /**
     * Command to pay for order.
     *
     * @param orderId order identifier
     * @param paymentId payment transaction identifier
     */
    public record PayOrderCommand(UUID orderId, String paymentId) {
        public PayOrderCommand {
            if (orderId == null) {
                throw new IllegalArgumentException("Order ID cannot be null");
            }
            if (paymentId == null || paymentId.isBlank()) {
                throw new IllegalArgumentException("Payment ID cannot be null or blank");
            }
        }
    }
}