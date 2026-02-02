package com.consistencyapp.backend.controller.auth;

import com.consistencyapp.backend.dto.auth.AuthResponse;
import com.consistencyapp.backend.dto.auth.RegisterRequest;
import com.consistencyapp.backend.service.auth.AuthService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final AuthService authService;

    public RegistrationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
