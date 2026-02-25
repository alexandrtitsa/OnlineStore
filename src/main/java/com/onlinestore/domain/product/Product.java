package com.onlinestore.domain.product;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String name;
    private final BigDecimal price;
    private int stockQuantity;

    public Product(UUID id, String name, BigDecimal price, int stockQuantity) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }

    public void reserveStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public void releaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}