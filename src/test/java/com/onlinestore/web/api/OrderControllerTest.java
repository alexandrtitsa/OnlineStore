package com.onlinestore.web.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    ProductRepository productRepository;

    private String authHeader;
    private UUID productId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        registerUser();
        loginAndCaptureToken();
        persistProductDirectly();
    }

    // -------------------------
    // TESTS
    // -------------------------

    @Test
    void shouldRejectOrderWhenNotAuthenticated() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "items": [
                    {
                      "productId": "%s",
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(productId))
                .when()
                .post("/orders")
                .then()
                .statusCode(403);
    }

    // -------------------------
    // SETUP HELPERS
    // -------------------------

    private void registerUser() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "order@test.com",
                  "password": "password123"
                }
                """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(201), is(400))); // user may already exist
    }

    private void loginAndCaptureToken() {
        String accessToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {
                          "email": "order@test.com",
                          "password": "password123"
                        }
                        """)
                        .when()
                        .post("/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("accessToken");

        this.authHeader = "Bearer " + accessToken;
    }

    private void persistProductDirectly() {
        Product product = new Product(
                UUID.randomUUID(),
                "Test Product",
                BigDecimal.valueOf(100),
                10
        );

        productRepository.save(product);
        this.productId = product.getId();
    }
}
