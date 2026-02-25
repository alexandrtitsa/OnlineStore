package com.onlinestore.application.exception;

import java.util.UUID;

public class ProductNotFoundException extends ApplicationException {

    private final UUID productId;

    public ProductNotFoundException(UUID productId) {
        super("Product not found: " + productId);
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}