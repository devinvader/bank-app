package ru.devinvader.bank.commontest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.client.NotificationClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class CommonTestSecurityConfig {

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService authorizedClientService() {
        return mock(OAuth2AuthorizedClientService.class);
    }

    @Bean
    @Primary
    public AccountsClient accountsClient() {
        return mock(AccountsClient.class);
    }

    @Bean
    @Primary
    public NotificationClient notificationClient() {
        return mock(NotificationClient.class);
    }
}
