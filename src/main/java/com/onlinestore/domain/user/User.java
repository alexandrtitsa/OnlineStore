package com.onlinestore.domain.user;

import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String password; // BCrypt hashed
    private final Set<Role> roles;
    private final boolean enabled;

    public User(UUID id, String email, String password, Set<Role> roles, boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.enabled = enabled;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return roles; }
    public boolean isEnabled() { return enabled; }
}