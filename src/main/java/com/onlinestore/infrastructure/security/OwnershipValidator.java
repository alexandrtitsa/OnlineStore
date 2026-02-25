package com.onlinestore.infrastructure.security;

import com.onlinestore.infrastructure.security.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("ownershipValidator")
public class OwnershipValidator {

    public boolean isOwner(UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserId().equals(userId);
        }

        return false;
    }

    public boolean isOwnerOrAdmin(UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            boolean isOwner = jwtAuth.getUserId().equals(userId);
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            return isOwner || isAdmin;
        }

        return false;
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserId();
        }

        throw new IllegalStateException("User not authenticated");
    }
}