package com.consistencyapp.backend.controller.auth;

import com.consistencyapp.backend.dto.auth.AuthResponse;
import com.consistencyapp.backend.dto.auth.LoginRequest;
import com.consistencyapp.backend.service.auth.AuthService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
