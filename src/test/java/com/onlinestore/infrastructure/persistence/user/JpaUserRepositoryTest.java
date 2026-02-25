package com.onlinestore.infrastructure.persistence.user;

import com.onlinestore.AbstractIntegrationTestH2;
import com.onlinestore.domain.user.Role;
import com.onlinestore.domain.user.User;
import com.onlinestore.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JpaUserRepositoryTest extends AbstractIntegrationTestH2 {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User user = new User(
                UUID.randomUUID(),
                "test@example.com",
                "hashedPassword",
                Set.of(Role.USER),
                true
        );

        // When
        User saved = userRepository.save(user);

        // Then
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getRoles()).containsExactly(Role.USER);
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = new User(
                UUID.randomUUID(),
                "findme@example.com",
                "password",
                Set.of(Role.USER),
                true
        );
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("findme@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findme@example.com");
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        User user = new User(
                UUID.randomUUID(),
                "exists@example.com",
                "password",
                Set.of(Role.USER),
                true
        );
        userRepository.save(user);

        // When / Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("doesnotexist@example.com")).isFalse();
    }

    @Test
    void shouldSaveUserWithMultipleRoles() {
        // Given
        User admin = new User(
                UUID.randomUUID(),
                "admin@example.com",
                "password",
                Set.of(Role.USER, Role.ADMIN),
                true
        );

        // When
        User saved = userRepository.save(admin);

        // Then
        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        assertThat(found.isAdmin()).isTrue();
    }
}