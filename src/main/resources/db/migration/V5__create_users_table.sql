CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_roles (user_id UUID NOT NULL,
                         role VARCHAR(20) NOT NULL,
                         PRIMARY KEY (user_id, role),
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);