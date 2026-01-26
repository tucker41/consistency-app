package com.consistencyapp.backend.security;

import com.consistencyapp.backend.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        var header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring("Bearer ".length()).trim();

            try {
                Claims claims = jwtService.parseAndValidate(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Long userId = Long.valueOf(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String displayName = claims.get("displayName", String.class);

                    var principal = new AuthenticatedUser(userId, email, displayName);

                    var auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of()
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Invalid token -> no auth set; SecurityConfig will enforce auth where required
            }
        }

        filterChain.doFilter(request, response);
    }
}
