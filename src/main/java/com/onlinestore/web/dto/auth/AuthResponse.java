package com.onlinestore.web.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication response")
public record AuthResponse(
        @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Token expiration time in seconds", example = "900")
        long expiresIn
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMinutes) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresInMinutes * 60
        );
    }
}