package ru.devinvader.bank.transfer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.commontest.util.JwtTestUtils;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return JwtTestUtils.testJwtDecoder("transfer:execute transfer:read");
    }

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        return mock(WebClient.Builder.class);
    }
}
