package ru.devinvader.bank.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;

@SpringBootTest
@Import(AbstractTestcontainersConfiguration.class)
class TransferApplicationTests {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    @Test
    void contextLoads() {
    }

}
