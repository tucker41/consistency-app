package com.consistencyapp.backend.service.auth;

import com.consistencyapp.backend.dto.auth.AuthResponse;
import com.consistencyapp.backend.dto.auth.LoginRequest;
import com.consistencyapp.backend.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
}
