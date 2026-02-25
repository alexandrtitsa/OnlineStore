package com.onlinestore.infrastructure.persistence.user;

import com.onlinestore.domain.user.User;
import com.onlinestore.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing UserRepository (domain port).
 */
@Repository
@Transactional(readOnly = true)
class UserRepositoryAdapter implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    UserRepositoryAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding User: id={}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding User by email: email={}", email);

        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving User: id={}, email={}", user.getId(), user.getEmail());

        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        User result = mapper.toDomain(saved);

        log.debug("User saved: id={}", saved.getId());

        return result;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}