package org.iwouldargue.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.iwouldargue.config.AuthConfig;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenIssuer {

    private final AuthConfig config;

    public String issue(Session session) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claim("displayName", session.displayName())
                .claim("discussionId", session.discussionId())
                .claim("role", session.role())
                .claim("userId", session.userId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(config.getExpirationSeconds())))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(config.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
