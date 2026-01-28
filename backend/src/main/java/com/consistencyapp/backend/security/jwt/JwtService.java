package com.consistencyapp.backend.security.jwt;

import com.consistencyapp.backend.domain.entity.AppUser;
import com.consistencyapp.backend.security.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    public String createAccessToken(AppUser user) {
        return createAccessToken(user, List.of(UserRole.USER));
    }

    public String createAccessToken(AppUser user, List<UserRole> roles) {
        var now = Instant.now();
        var exp = now.plusSeconds(props.accessTokenMinutes() * 60);

        var key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));

        List<String> roleNames = new java.util.ArrayList<>();
        if (roles != null) {
            for (UserRole r : roles) {
                if (r != null) roleNames.add(r.name());
            }
        }






        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("displayName", user.getDisplayName())
                .claim("roles", roleNames)
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
