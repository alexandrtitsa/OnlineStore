CREATE TABLE products (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                          stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
                          version BIGINT NOT NULL DEFAULT 0
);