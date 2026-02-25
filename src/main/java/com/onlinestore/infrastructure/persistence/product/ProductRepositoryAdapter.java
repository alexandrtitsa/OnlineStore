package com.onlinestore.infrastructure.persistence.product;

import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ProductRepository (domain port).
 */
@Repository
@Transactional(readOnly = true)
class ProductRepositoryAdapter implements ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductRepositoryAdapter.class);

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("Finding Product: id={}", id);

        return jpaRepository.findById(id)
                .map(entity -> {
                    Product product = mapper.toDomain(entity);
                    log.debug("Product found: id={}, stock={}", id, product.getStockQuantity());
                    return product;
                });
    }

    @Override
    @Transactional
    public Product save(Product product) {
        log.debug("Saving Product: id={}, stock={}",
                product.getId(), product.getStockQuantity());

        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        Product result = mapper.toDomain(saved);

        log.debug("Product saved: id={}, version={}", saved.getId(), saved.getVersion());

        return result;
    }
}