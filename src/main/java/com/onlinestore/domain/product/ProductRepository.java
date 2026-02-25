package com.onlinestore.domain.product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    Product save(Product product);
}