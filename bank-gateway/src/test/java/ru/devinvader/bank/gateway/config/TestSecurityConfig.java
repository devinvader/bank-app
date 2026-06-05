package ru.devinvader.bank.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> Mono.just(Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("scope", "accounts:read accounts:write cash:operate")
                .claim("preferred_username", "user1")
                .claim("sub", "user1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build());
    }
}
