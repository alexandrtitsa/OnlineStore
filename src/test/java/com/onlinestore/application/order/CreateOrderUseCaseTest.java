package com.onlinestore.application.order;

import com.onlinestore.domain.event.DomainEvent;
import com.onlinestore.domain.event.DomainEventPublisher;
import com.onlinestore.domain.order.*;
import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import com.onlinestore.application.exception.OrderOperationException;
import com.onlinestore.application.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CreateOrderUseCase.
 * Uses in-memory implementations to test business logic in isolation.
 */
class CreateOrderUseCaseTest {

    private InMemoryOrderRepository orderRepository;
    private InMemoryProductRepository productRepository;
    private TestEventPublisher eventPublisher;
    private OrderPolicy orderPolicy;
    private CreateOrderUseCase useCase;

    @BeforeEach
    void setup() {
        orderRepository = new InMemoryOrderRepository();
        productRepository = new InMemoryProductRepository();
        eventPublisher = new TestEventPublisher();
        orderPolicy = new DefaultOrderPolicy(); // ← Add OrderPolicy

        useCase = new CreateOrderUseCase(
                orderRepository,
                productRepository,
                eventPublisher,
                orderPolicy // ← Pass to constructor
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Laptop", BigDecimal.valueOf(999.99), 10);
        productRepository.save(product);

        UUID userId = UUID.randomUUID();
        var command = new CreateOrderUseCase.CreateOrderCommand(
                userId,
                List.of(new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(productId, 2))
        );

        // When
        UUID orderId = useCase.execute(command);

        // Then
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("1999.98");

        // Stock should be reserved
        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(8);

        // Events should be published
        assertThat(eventPublisher.publishedEvents).isNotEmpty();
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Mouse", BigDecimal.valueOf(29.99), 5);
        productRepository.save(product);

        UUID userId = UUID.randomUUID();
        var command = new CreateOrderUseCase.CreateOrderCommand(
                userId,
                List.of(new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(productId, 10))
        );

        // When / Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(OrderOperationException.class) // ← Wrapped in application exception
                .hasMessageContaining("requested 10, available 5");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();
        var command = new CreateOrderUseCase.CreateOrderCommand(
                userId,
                List.of(new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(nonExistentProductId, 1))
        );

        // When / Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFoundException.class) // ← Application exception
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldCreateOrderWithMultipleItems() {
        // Given
        UUID product1Id = UUID.randomUUID();
        UUID product2Id = UUID.randomUUID();

        Product product1 = new Product(product1Id, "Laptop", BigDecimal.valueOf(999.99), 10);
        Product product2 = new Product(product2Id, "Mouse", BigDecimal.valueOf(29.99), 20);

        productRepository.save(product1);
        productRepository.save(product2);

        UUID userId = UUID.randomUUID();
        var command = new CreateOrderUseCase.CreateOrderCommand(
                userId,
                List.of(
                        new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(product1Id, 1),
                        new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(product2Id, 2)
                )
        );

        // When
        UUID orderId = useCase.execute(command);

        // Then
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("1059.97"); // 999.99 + 2*29.99

        // Both products should have stock reserved
        assertThat(productRepository.findById(product1Id).orElseThrow().getStockQuantity()).isEqualTo(9);
        assertThat(productRepository.findById(product2Id).orElseThrow().getStockQuantity()).isEqualTo(18);
    }

    @Test
    void shouldPublishDomainEventsAfterOrderCreation() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Product", BigDecimal.TEN, 10);
        productRepository.save(product);

        UUID userId = UUID.randomUUID();
        var command = new CreateOrderUseCase.CreateOrderCommand(
                userId,
                List.of(new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(productId, 1))
        );

        // When
        UUID orderId = useCase.execute(command);

        // Then
        assertThat(eventPublisher.publishedEvents)
                .isNotEmpty()
                .anyMatch(event -> event.orderId().equals(orderId));
    }

    // ==================== IN-MEMORY IMPLEMENTATIONS ====================

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<UUID, Order> storage = new HashMap<>();

        @Override
        public Order save(Order order) {
            storage.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Order> findByUserId(UUID userId) {
            return storage.values().stream()
                    .filter(o -> o.getUserId().equals(userId))
                    .toList();
        }

        @Override
        public void delete(UUID id) {
            storage.remove(id);
        }
    }

    static class InMemoryProductRepository implements ProductRepository {
        private final Map<UUID, Product> storage = new HashMap<>();

        @Override
        public Product save(Product product) {
            storage.put(product.getId(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }
    }

    static class TestEventPublisher implements DomainEventPublisher {
        final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            publishedEvents.add(event);
        }
    }
}