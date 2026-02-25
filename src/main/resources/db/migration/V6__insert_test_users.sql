-- Password: password123 (BCrypt)
INSERT INTO users (id, email, password, enabled) VALUES
                                                     (gen_random_uuid(), 'user@example.com',
                                                      '$2a$10$L1oLDhRLkYqLvem7USE7yuqpvBqvJskl6jDXNb/R6ebXDcPG3NRF6', true),
                                                     (gen_random_uuid(), 'admin@example.com',
                                                      '$2a$10$L1oLDhRLkYqLvem7USE7yuqpvBqvJskl6jDXNb/R6ebXDcPG3NRF6', true);

INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users WHERE email = 'user@example.com'
UNION ALL
SELECT id, 'USER' FROM users WHERE email = 'admin@example.com'
UNION ALL
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@example.com';