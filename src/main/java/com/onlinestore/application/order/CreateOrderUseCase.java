package com.onlinestore.application.order;

import com.onlinestore.application.exception.OrderOperationException;
import com.onlinestore.application.exception.ProductNotFoundException;
import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.exception.InsufficientStockException;
import com.onlinestore.domain.exception.InvalidOrderStateException;
import com.onlinestore.domain.order.*;
import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use Case: Create New Order
 *
 * RESPONSIBILITIES:
 * - Orchestrate order creation workflow
 * - Validate product availability
 * - Reserve product stock
 * - Create and persist Order aggregate
 * - Publish domain events
 *
 * TRANSACTION BOUNDARY:
 * - This method defines the transaction boundary
 * - All repository operations are within single transaction
 * - Events are published AFTER successful commit (via TransactionalEventListener)
 *
 * EXCEPTION HANDLING:
 * - Domain exceptions are wrapped in application exceptions
 * - Application exceptions are mapped to HTTP responses by web layer
 */
public class CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    private final OrderPolicy orderPolicy;

    public CreateOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            DomainEventPublisher eventPublisher,
            OrderPolicy orderPolicy
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.orderPolicy = orderPolicy;
    }

    /**
     * Executes order creation workflow.
     *
     * WORKFLOW:
     * 1. Validate product availability and reserve stock
     * 2. Create Order aggregate with items
     * 3. Validate business rules (via OrderPolicy)
     * 4. Transition to PENDING_PAYMENT status
     * 5. Save complete aggregate (atomic operation)
     * 6. Publish domain events (after commit)
     *
     * TRANSACTIONAL GUARANTEES:
     * - All operations succeed or all rollback
     * - Stock reservation is atomic with order creation
     * - Events published only after successful commit
     *
     * @param command order creation command
     * @return ID of created order
     * @throws ProductNotFoundException if product not found
     * @throws OrderOperationException if order creation fails
     */
    @Transactional
    public UUID execute(CreateOrderCommand command) {
        log.info("Creating order for user: userId={}, itemCount={}",
                command.userId(), command.items().size());

        try {
            // Step 1: Validate and reserve stock
            List<OrderItem> orderItems = new ArrayList<>();
            List<Product> products = new ArrayList<>();

            for (var item : command.items()) {
                // Find product (throws ProductNotFoundException if not found)
                Product product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new ProductNotFoundException(item.productId()));

                // Validate stock availability (throws InsufficientStockException)
                if (!product.hasStock(item.quantity())) {
                    throw new InsufficientStockException(
                            product.getId(),
                            item.quantity(),
                            product.getStockQuantity()
                    );
                }

                // Reserve stock
                product.reserveStock(item.quantity());
                products.add(product);

                // Build order item
                orderItems.add(new OrderItem(
                        product.getId(),
                        product.getName(),
                        item.quantity(),
                        product.getPrice()
                ));
            }

            // Step 2: Create Order aggregate
            Order order = Order.create(command.userId(), orderItems);

            // Step 3: Validate business rules
            orderPolicy.validateConfirmation(order);

            // Step 4: Transition to PENDING_PAYMENT
            order.markAsPendingPayment();

            // Step 5: Save all (atomic within transaction)
            // Note: orderRepository.save() persists COMPLETE aggregate (Order + all items)
            for (Product product : products) {
                productRepository.save(product);
            }
            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully: orderId={}, status={}, total={}",
                    savedOrder.getId(), savedOrder.getStatus(), savedOrder.getTotalAmount());

            // Step 6: Publish domain events
            // Events will be sent AFTER transaction commit (TransactionalEventListener)
            savedOrder.getDomainEvents().forEach(event -> {
                log.debug("Queueing event for publication: {}", event.getClass().getSimpleName());
                eventPublisher.publish(event);
            });
            savedOrder.clearDomainEvents();

            return savedOrder.getId();

            // Transaction commits here ↑
            // Events are actually published AFTER commit

        } catch (InsufficientStockException e) {
            // Wrap domain exception in application exception
            log.warn("Order creation failed - insufficient stock: {}", e.getMessage());
            throw new OrderOperationException(
                    "Cannot create order: " + e.getMessage(), e
            );
        } catch (InvalidOrderStateException e) {
            // Wrap domain exception in application exception
            log.warn("Order creation failed - invalid state: {}", e.getMessage());
            throw new OrderOperationException(
                    "Invalid order operation: " + e.getMessage(), e
            );
        } catch (ProductNotFoundException e) {
            // Application exception - just rethrow
            log.warn("Order creation failed - product not found: {}", e.getProductId());
            throw e;
        } catch (Exception e) {
            // Unexpected error
            log.error("Order creation failed with unexpected error", e);
            throw new OrderOperationException(
                    "Order creation failed due to unexpected error", e
            );
        }
    }

    /**
     * Command to create order.
     *
     * @param userId user creating the order
     * @param items list of items to include in order
     */
    public record CreateOrderCommand(
            UUID userId,
            List<OrderItemDto> items
    ) {
        /**
         * Order item DTO.
         *
         * @param productId product identifier
         * @param quantity quantity to order
         */
        public record OrderItemDto(UUID productId, int quantity) {}
    }
}