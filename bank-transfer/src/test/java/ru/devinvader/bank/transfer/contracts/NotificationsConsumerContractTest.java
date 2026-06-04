package ru.devinvader.bank.transfer.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration"
})
@AutoConfigureStubRunner(
    ids = "ru.devinvader.bank.notifications:bank-notifications:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class NotificationsConsumerContractTest {

    @Autowired
    private StubFinder stubFinder;

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
