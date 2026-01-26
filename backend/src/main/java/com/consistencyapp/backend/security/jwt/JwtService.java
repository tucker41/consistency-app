package com.consistencyapp.backend.security.jwt;

import com.consistencyapp.backend.domain.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    public String createAccessToken(AppUser user) {
        var now = Instant.now();
        var exp = now.plusSeconds(props.accessTokenMinutes() * 60);

        var key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("displayName", user.getDisplayName())
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        var key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long accessTokenExpiresInSeconds() {
        return props.accessTokenMinutes() * 60;
    }
}
