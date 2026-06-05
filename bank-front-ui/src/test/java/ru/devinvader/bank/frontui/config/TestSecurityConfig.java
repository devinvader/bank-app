package ru.devinvader.bank.frontui.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import ru.devinvader.bank.frontui.service.TokenProvider;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("scope", "accounts:read accounts:write cash:operate transfer:execute")
                .claim("preferred_username", "user1")
                .claim("sub", "user1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        var repo = mock(ClientRegistrationRepository.class);
        var registration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("bank-front-ui")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("http://localhost:8089/realms/bank/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8089/realms/bank/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8089/realms/bank/protocol/openid-connect/userinfo")
                .userNameAttributeName("preferred_username")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .build();
        when(repo.findByRegistrationId("keycloak")).thenReturn(registration);
        return repo;
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService authorizedClientService() {
        var service = Mockito.mock(OAuth2AuthorizedClientService.class);
        var token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "test-token", Instant.now(), Instant.now().plusSeconds(3600));
        var client = new OAuth2AuthorizedClient(mock(ClientRegistration.class), "user1", token, null);
        when(service.loadAuthorizedClient(eq("keycloak"), eq("user1"))).thenReturn(client);
        return service;
    }

    @Bean
    @Primary
    public TokenProvider tokenProvider(OAuth2AuthorizedClientService authorizedClientService) {
        var provider = Mockito.mock(TokenProvider.class);
        when(provider.getAccessToken()).thenReturn("test-token");
        when(provider.getUsername()).thenReturn("user1");
        return provider;
    }
}
