package ru.devinvader.bank.accounts.contracts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.accounts.integration.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureStubRunner(
    ids = "ru.devinvader.bank.notifications:bank-notifications:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
class NotificationsConsumerContractTest {

    @Autowired
    private StubFinder stubFinder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode(anyString())).thenReturn(
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .claim("scope", "accounts:read accounts:write")
                        .claim("preferred_username", "user1")
                        .claim("sub", "user1")
                        .issuedAt(java.time.Instant.now())
                        .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        .build()
        );
    }

    @Test
    void shouldSendNotificationViaContract() {
        var stubUrl = stubFinder.findStubUrl("bank-notifications").toString();
        var webClient = WebClient.builder().baseUrl(stubUrl).build();

        var response = webClient.post()
                .uri("/api/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "type", "TRANSFER",
                        "accountId", "user123",
                        "amount", 100.50,
                        "message", "Transfer from user456"
                ))
                .retrieve()
                .toEntity(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("SENT");
    }

    @Test
    void shouldReturn401WhenNoToken() {
        var stubUrl = stubFinder.findStubUrl("bank-notifications").toString();
        var webClient = WebClient.builder().baseUrl(stubUrl).build();

        var exception = assertThrows(WebClientResponseException.class, () ->
                webClient.post()
                        .uri("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "type", "TRANSFER",
                                "accountId", "user123",
                                "amount", 100.50,
                                "message", "Test"
                        ))
                        .retrieve()
                        .toEntity(String.class)
                        .block()
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(401);
    }
}
