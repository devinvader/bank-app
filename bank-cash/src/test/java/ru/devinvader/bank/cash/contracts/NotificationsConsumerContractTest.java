package ru.devinvader.bank.cash.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

class NotificationsConsumerContractTest extends CashControllerBase {

    @Test
    void shouldSendNotificationViaContract() {
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
    void shouldReturn401WhenNoToken() {
        URL stubUrl = stubFinder.findStubUrl("bank-notifications");
        var restTemplate = new RestTemplate();

        var requestBody = """
            {
                "type": "TRANSFER",
                "accountId": "user123",
                "amount": 100.50,
                "message": "Test"
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
