package ru.devinvader.bank.common.config;

import java.time.Duration;

import io.micrometer.observation.ObservationRegistry;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@AutoConfiguration
public class WebClientAutoConfiguration {

    @Bean
    @ConditionalOnBean({ClientRegistrationRepository.class, OAuth2AuthorizedClientService.class})
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder(
            ObjectProvider<OAuth2AuthorizedClientManager> authorizedClientManagerProvider,
            ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        var httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        var builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
        var observationRegistry = observationRegistryProvider.getIfAvailable();
        if (observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        var authorizedClientManager = authorizedClientManagerProvider.getIfAvailable();
        if (authorizedClientManager != null) {
            var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
            oauth2Client.setDefaultClientRegistrationId("keycloak");
            builder.filter(oauth2Client);
        }
        return builder;
    }
}
