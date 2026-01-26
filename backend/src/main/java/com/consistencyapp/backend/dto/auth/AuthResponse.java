package com.consistencyapp.backend.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserSummary user
) {
    public record UserSummary(Long id, String email, String displayName) {}
}
