package com.onlinestore.web.api;

import com.onlinestore.application.user.LoginUseCase;
import com.onlinestore.application.user.RegisterUserUseCase;
import com.onlinestore.web.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account and receive JWT tokens for authentication"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 900
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "status": 400,
                        "error": "Validation Failed",
                        "validationErrors": {
                            "email": "Invalid email format",
                            "password": "Password must be at least 8 characters"
                        },
                        "timestamp": "2026-01-22T12:00:00Z"
                    }
                    """)
                    )
            )
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "email": "user@example.com",
                        "password": "password123"
                    }
                    """)
                    )
            )
            RegisterRequest request
    ) {
        var command = new RegisterUserUseCase.RegisterCommand(
                request.email(),
                request.password()
        );

        AuthResponse response = registerUserUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user and receive JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Invalid credentials",
                        "timestamp": "2026-01-22T12:00:00Z"
                    }
                    """)
                    )
            )
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "email": "user@example.com",
                        "password": "password123"
                    }
                    """)
                    )
            )
            LoginRequest request
    ) {
        var command = new LoginUseCase.LoginCommand(
                request.email(),
                request.password()
        );

        AuthResponse response = loginUseCase.execute(command);

        return ResponseEntity.ok(response);
    }
}