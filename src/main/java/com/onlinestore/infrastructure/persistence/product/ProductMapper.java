package com.onlinestore.infrastructure.persistence.product;

import com.onlinestore.domain.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductEntity toEntity(Product domain) {
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setPrice(domain.getPrice());
        entity.setStockQuantity(domain.getStockQuantity());
        return entity;
    }

    public Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getStockQuantity()
        );
    }
}