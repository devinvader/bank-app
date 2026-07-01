package ru.devinvader.bank.commontest.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

public final class JwtTestUtils {

    public static final String TEST_SUBJECT = "afd94176-3179-4285-9f6b-96fd9131628a";

    private JwtTestUtils() {
    }

    public static Jwt createTestJwt(String tokenValue, String scopes) {
        return Jwt.withTokenValue(tokenValue)
                .header("alg", "none")
                .claim("scope", scopes)
                .claim("sub", TEST_SUBJECT)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    public static JwtDecoder testJwtDecoder(String scopes) {
        return token -> createTestJwt(token, scopes);
    }

    public static ReactiveJwtDecoder testReactiveJwtDecoder(String scopes) {
        return token -> Mono.just(createTestJwt(token, scopes));
    }
}
