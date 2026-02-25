package com.onlinestore.infrastructure.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * JPA repository for ProductEntity (infrastructure layer).
 */
interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
}