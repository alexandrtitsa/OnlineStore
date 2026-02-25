package com.onlinestore.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal pricePerUnit;

    public OrderItem(UUID productId, String productName, int quantity, BigDecimal pricePerUnit) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public BigDecimal getSubtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }
}