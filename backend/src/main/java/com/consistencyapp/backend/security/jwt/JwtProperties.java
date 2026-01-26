package com.consistencyapp.backend.security.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Min(1) long accessTokenMinutes
) {}
