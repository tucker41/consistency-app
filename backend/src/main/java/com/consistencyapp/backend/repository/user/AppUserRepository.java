package com.consistencyapp.backend.repository.user;

import com.consistencyapp.backend.domain.entity.AppUser;
import com.consistencyapp.backend.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDisplayNameIgnoreCase(String displayName);

    boolean existsByUsernameIgnoreCase(String username);

    java.util.Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);
}
