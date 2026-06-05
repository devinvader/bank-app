package ru.devinvader.bank.transfer.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.transfer.integration.TestcontainersConfiguration;

import java.net.URL;

@SpringBootTest
@AutoConfigureStubRunner(
    ids = "ru.devinvader.bank.notifications:bank-notifications:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(TestcontainersConfiguration.class)
class NotificationsConsumerContractTest {

    @Autowired
    private StubFinder stubFinder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    @Test
    void shouldCreateNotificationViaContract() {
        URL stubUrl = stubFinder.findStubUrl("bank-notifications");
        var restTemplate = new RestTemplate();

        var requestBody = """
            {
                "type": "TRANSFER",
                "accountId": "user123",
                "amount": 100.50,
                "message": "Transfer from user456"
            }
            """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            stubUrl + "/api/notifications", entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("SENT");
    }

    @Test
    void shouldFailWhenNoToken() {
        URL stubUrl = stubFinder.findStubUrl("bank-notifications");
        var restTemplate = new RestTemplate();

        var requestBody = """
            {
                "type": "TRANSFER",
                "accountId": "user123",
                "amount": 50.00,
                "message": "Test without token"
            }
            """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(requestBody, headers);

        HttpClientErrorException exception = assertThrows(
            HttpClientErrorException.class,
            () -> restTemplate.postForEntity(
                stubUrl + "/api/notifications", entity, String.class));

        assertThat(exception.getStatusCode().value()).isEqualTo(401);
    }
}
