package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public String getAccessToken() {
        var auth = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var client = authorizedClientService.loadAuthorizedClient("keycloak", auth.getName());
        return client.getAccessToken().getTokenValue();
    }

    public String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
