package com.onlinestore.infrastructure.persistence.product;

import com.onlinestore.AbstractIntegrationTestH2;
import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaProductRepositoryTest extends AbstractIntegrationTestH2 {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product product = new Product(
                UUID.randomUUID(),
                "Test Product",
                BigDecimal.valueOf(99.99),
                50
        );

        // When
        Product saved = productRepository.save(product);

        // Then
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getPrice()).isEqualByComparingTo("99.99");
        assertThat(found.get().getStockQuantity()).isEqualTo(50);
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        // When
        Optional<Product> found = productRepository.findById(UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }
}