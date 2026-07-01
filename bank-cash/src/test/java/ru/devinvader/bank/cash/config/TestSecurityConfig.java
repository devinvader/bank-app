package ru.devinvader.bank.cash.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import ru.devinvader.bank.commontest.config.CommonTestSecurityConfig;
import ru.devinvader.bank.commontest.util.JwtTestUtils;

@TestConfiguration
@Import(CommonTestSecurityConfig.class)
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return JwtTestUtils.testJwtDecoder("cash:operate");
    }
}
