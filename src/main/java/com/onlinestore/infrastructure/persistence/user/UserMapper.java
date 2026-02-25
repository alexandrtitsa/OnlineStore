package com.onlinestore.infrastructure.persistence.user;

import com.onlinestore.domain.user.Role;
import com.onlinestore.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRoles(domain.getRoles().stream()
                .map(this::mapRole)
                .collect(Collectors.toSet()));
        entity.setEnabled(domain.isEnabled());
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRoles().stream()
                        .map(this::mapRole)
                        .collect(Collectors.toSet()),
                entity.isEnabled()
        );
    }

    private RoleEntity mapRole(Role domain) {
        return RoleEntity.valueOf(domain.name());
    }

    private Role mapRole(RoleEntity entity) {
        return Role.valueOf(entity.name());
    }
}