package org.iwouldargue.gateway;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    private final JwtConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.error("Filter hit");

        var authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            return chain.filter(exchange);
        }

        var token = authHeader.substring(7);

        Claims claims;
        try {
            claims = (Claims) Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parse(token)
                    .getBody();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Session-Display-Name", claims.get("displayName", String.class))
                .header("X-Session-Discussion-Id", claims.get("discussionId", String.class))
                .header("X-Session-Role", claims.get("role", String.class))
                .header("X-Session-User-Id", claims.get("userId", String.class))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(config.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}