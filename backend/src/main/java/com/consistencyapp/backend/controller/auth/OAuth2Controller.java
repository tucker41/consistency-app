package com.consistencyapp.backend.controller.auth;

import com.consistencyapp.backend.domain.entity.AppUser;
import com.consistencyapp.backend.domain.enums.AuthProvider;
import com.consistencyapp.backend.dto.auth.AuthResponse;
import com.consistencyapp.backend.exception.ConflictException;
import com.consistencyapp.backend.exception.UnauthorizedException;
import com.consistencyapp.backend.repository.user.AppUserRepository;
import com.consistencyapp.backend.security.jwt.JwtService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2Controller {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    public OAuth2Controller(AppUserRepository appUserRepository, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/api/auth/oauth2/success")
    @Transactional
    public AuthResponse success(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) {
            throw new UnauthorizedException("Not authenticated with OAuth2.");
        }

        String email = normalizeEmail(getStringAttr(oauthUser, "email"));
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("OAuth2 account has no email.");
        }

        String providerId = getStringAttr(oauthUser, "sub");
        if (providerId == null || providerId.isBlank()) {
            providerId = email;
        }

        AppUser user = appUserRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE, providerId).orElse(null);

        if (user == null) {
            user = createGoogleUser(oauthUser, email, providerId);
        }

        String token = jwtService.createAccessToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.accessTokenExpiresInSeconds(),
                new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName())
        );
    }

    private AppUser createGoogleUser(OAuth2User oauthUser, String email, String providerId) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered. Please login with email/password.");
        }

        String displayName = normalizeDisplayName(getStringAttr(oauthUser, "name"));
        if (displayName == null || displayName.isBlank()) {
            displayName = email.substring(0, email.indexOf('@'));
        }

        String suggestedUsername = suggestUsernameFromEmail(email);
        String uniqueUsername = makeUniqueUsername(suggestedUsername);

        var created = new AppUser();
        created.setEmail(email);
        created.setAuthProvider(AuthProvider.GOOGLE);
        created.setProviderId(providerId);

        created.setUsername(uniqueUsername);
        created.setDisplayName(displayName);

        created.setPasswordHash(null);

        return appUserRepository.save(created);
    }

    private String getStringAttr(OAuth2User user, String key) {
        Object v = user.getAttributes().get(key);
        return v == null ? null : String.valueOf(v);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeDisplayName(String displayName) {
        return displayName == null ? null : displayName.trim();
    }

    private String suggestUsernameFromEmail(String email) {
        String localPart = email.substring(0, email.indexOf('@'));

        String base = localPart.trim().toLowerCase()
                .replaceAll("[^a-z0-9._]", "");

        if (base.length() < 3) {
            base = "user";
        }

        if (base.length() > 30) {
            base = base.substring(0, 30);
        }

        return base;
    }

    private String makeUniqueUsername(String base) {
        String candidate = base;
        int suffix = 0;

        while (appUserRepository.existsByUsernameIgnoreCase(candidate)) {
            suffix++;
            String suffixStr = String.valueOf(suffix);

            int maxBaseLen = 30 - suffixStr.length();
            String trimmedBase = base.length() > maxBaseLen ? base.substring(0, maxBaseLen) : base;

            candidate = trimmedBase + suffixStr;
        }

        return candidate;
    }
}
