package com.onlinestore.application.user;

import com.onlinestore.domain.user.User;
import com.onlinestore.domain.user.UserRepository;
import com.onlinestore.infrastructure.security.jwt.JwtTokenService;
import com.onlinestore.web.dto.auth.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public LoginUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse execute(LoginCommand command) {

        log.debug("Login attempt for email: {}", command.email());

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email={}", command.email());
                    return new IllegalArgumentException("Invalid credentials");
                });

        log.debug("User found: id={}, enabled={}, roles={}",
                user.getId(),
                user.isEnabled(),
                user.getRoles()
        );

        String rawPassword = command.password();
        String encodedPassword = user.getPassword();

        log.debug("Password check: rawLength={}, encodedStartsWithBcrypt={}",
                rawPassword != null ? rawPassword.length() : null,
                encodedPassword != null && encodedPassword.startsWith("$2")
        );

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        log.debug("Password matches result: {}", matches);

        if (!matches) {
            log.warn("Login failed: password mismatch for email={}", command.email());
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            log.warn("Login failed: account disabled for email={}", command.email());
            throw new IllegalArgumentException("Account is disabled");
        }

        log.debug("Generating JWT tokens for userId={}", user.getId());

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );

        String refreshToken = jwtTokenService.generateRefreshToken(user.getId());

        log.info("Login successful for email={}", command.email());

        return AuthResponse.of(accessToken, refreshToken, 15);
    }

    public record LoginCommand(String email, String password) {}
}