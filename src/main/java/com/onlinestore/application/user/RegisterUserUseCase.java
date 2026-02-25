package com.onlinestore.application.user;

import com.onlinestore.domain.user.Role;
import com.onlinestore.domain.user.User;
import com.onlinestore.domain.user.UserRepository;
import com.onlinestore.infrastructure.security.jwt.JwtTokenService;
import com.onlinestore.web.dto.auth.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse execute(RegisterCommand command) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create user
        User user = new User(
                UUID.randomUUID(),
                command.email(),
                passwordEncoder.encode(command.password()),
                Set.of(Role.USER), // Default role
                true
        );

        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenService.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRoles()
        );
        String refreshToken = jwtTokenService.generateRefreshToken(savedUser.getId());

        return AuthResponse.of(accessToken, refreshToken, 15);
    }

    public record RegisterCommand(String email, String password) {}
}