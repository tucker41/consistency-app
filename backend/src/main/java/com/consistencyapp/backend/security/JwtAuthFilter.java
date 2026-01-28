package com.consistencyapp.backend.security;

import com.consistencyapp.backend.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
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
            if (token.isEmpty()) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
                return;
            }

            try {
                Claims claims = jwtService.parseAndValidate(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Long userId = Long.valueOf(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String displayName = claims.get("displayName", String.class);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    List<?> rolesRaw = claims.get("roles", List.class);

                    if (rolesRaw != null) {
                        for (Object r : rolesRaw) {
                            if (r == null) continue;
                            String role = r.toString().trim();
                            if (role.isEmpty()) continue;

                            if (!role.startsWith("ROLE_")) role = "ROLE_" + role;

                            authorities.add(new SimpleGrantedAuthority(role));
                        }
                    }

                    var principal = new AuthenticatedUser(userId, email, displayName);

                    var auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
