package com.consistencyapp.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Username may only contain letters, numbers, '.' and '_' (no spaces).")
        String username,
        @NotBlank @Size(min = 1, max = 80) String displayName
) {}
