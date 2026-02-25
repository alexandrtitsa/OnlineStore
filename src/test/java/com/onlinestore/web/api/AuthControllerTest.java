package com.onlinestore.web.api;

import com.onlinestore.domain.user.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Sql(
        statements = "DELETE FROM users",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/auth";
    }

    @Test
    void shouldRegisterNewUser() {
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest(email, "password123"))
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0));
    }


    @Test
    void shouldRejectRegistrationWithShortPassword() {
        given()
                .contentType(ContentType.JSON)
                .body(registerRequest(uniqueEmail(), "short"))
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("validationErrors.password", containsString("at least 8 characters"));
    }

    @Test
    void shouldRejectRegistrationWithInvalidEmail() {
        given()
                .contentType(ContentType.JSON)
                .body(registerRequest("invalid-email", "password123"))
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("validationErrors.email", notNullValue());
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Test
    void shouldLoginWithValidCredentials() {
        String email = uniqueEmail();
        String password = "password123";

        register(email, password);

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest(email, password))
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"));
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .body(loginRequest("nonexistent@test.com", "wrongpassword"))
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Invalid credentials"));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void register(String email, String password) {
        given()
                .contentType(ContentType.JSON)
                .body(registerRequest(email, password))
                .when()
                .post("/register")
                .then()
                .statusCode(201);
    }

    private String registerRequest(String email, String password) {
        return """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);
    }

    private String loginRequest(String email, String password) {
        return """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@test.com";
    }
}
