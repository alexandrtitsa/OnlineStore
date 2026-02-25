package com.onlinestore;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests using H2 in-memory database.
 * Use this when Docker/Testcontainers is not available.
 *
 * For production-like testing with PostgreSQL, use AbstractIntegrationTest instead.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-h2")
public abstract class AbstractIntegrationTestH2 {
    // No Testcontainers setup needed - H2 is in-memory
}