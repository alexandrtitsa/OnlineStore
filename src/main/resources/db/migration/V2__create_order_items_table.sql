CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id UUID NOT NULL,
                             product_id UUID NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             quantity INT NOT NULL CHECK (quantity > 0),
                             price_per_unit DECIMAL(10, 2) NOT NULL CHECK (price_per_unit >= 0),
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);