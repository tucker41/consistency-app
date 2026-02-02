package com.consistencyapp.backend.security;

public record AuthenticatedUser(Long id, String email, String displayName) {}
