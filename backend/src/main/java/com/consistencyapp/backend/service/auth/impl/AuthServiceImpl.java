package com.consistencyapp.backend.service.auth.impl;

import com.consistencyapp.backend.domain.entity.AppUser;
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

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered.");
        }

        var user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(req.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));

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

        var user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

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
}
