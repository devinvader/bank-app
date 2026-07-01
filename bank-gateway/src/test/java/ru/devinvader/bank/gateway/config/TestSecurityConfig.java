package ru.devinvader.bank.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import ru.devinvader.bank.commontest.util.JwtTestUtils;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return JwtTestUtils.testReactiveJwtDecoder("accounts:read accounts:write cash:operate");
    }
}
