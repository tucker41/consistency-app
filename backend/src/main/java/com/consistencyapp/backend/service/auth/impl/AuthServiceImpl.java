package com.consistencyapp.backend.service.auth.impl;

import com.consistencyapp.backend.domain.entity.AppUser;
import com.consistencyapp.backend.domain.enums.AuthProvider;
import com.consistencyapp.backend.dto.auth.AuthResponse;
import com.consistencyapp.backend.dto.auth.LoginRequest;
import com.consistencyapp.backend.dto.auth.RegisterRequest;
import com.consistencyapp.backend.exception.ConflictException;
import com.consistencyapp.backend.exception.UnauthorizedException;
import com.consistencyapp.backend.repository.user.AppUserRepository;
import com.consistencyapp.backend.security.jwt.JwtService;
import com.consistencyapp.backend.service.auth.AuthService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int USERNAME_MIN = 3;
    private static final int USERNAME_MAX = 30;

    private static final String USERNAME_REGEX = "^[a-z0-9._]{3,30}$";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (email == null || email.isBlank()) {
            throw new ConflictException("Email is required.");
        }

        String username = normalizeUsername(req.username());
        validateUsername(username);

        String displayName = normalizeDisplayName(req.displayName());
        if (displayName == null || displayName.isBlank()) {
            throw new ConflictException("Display name is required.");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered.");
        }

        // Requires: existsByUsernameIgnoreCase in AppUserRepository
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already taken.");
        }

        var user = new AppUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(displayName);

        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setProviderId(null);

        user = appUserRepository.save(user);

        String token = jwtService.createAccessToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.accessTokenExpiresInSeconds(),
                new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.email());
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        var user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        // Only allow password login for local users
        if (user.getAuthProvider() != AuthProvider.LOCAL || user.getPasswordHash() == null) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtService.createAccessToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.accessTokenExpiresInSeconds(),
                new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName())
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null) return null;
        return username.trim().toLowerCase();
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ConflictException("Username is required.");
        }
        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            throw new ConflictException("Username must be between " + USERNAME_MIN + " and " + USERNAME_MAX + " characters.");
        }
        if (!username.matches(USERNAME_REGEX)) {
            throw new ConflictException("Username may only contain lowercase letters, numbers, '.' and '_' (no spaces).");
        }
    }

    private String normalizeDisplayName(String displayName) {
        return displayName == null ? null : displayName.trim();
    }
}
