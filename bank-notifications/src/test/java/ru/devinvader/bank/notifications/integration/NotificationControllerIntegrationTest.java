package ru.devinvader.bank.notifications.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createNotification_withValidData_shouldReturnOk() {
        var requestBody = """
                {
                    "type": "TRANSFER",
                    "accountId": "afd94176-3179-4285-9f6b-96fd9131628a",
                    "amount": 123.45,
                    "message": "блаблабла"
                }
                """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.postForEntity(
                "/api/notifications", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("SENT");
        assertThat(response.getBody()).contains("TRANSFER");
        assertThat(response.getBody()).contains("afd94176-3179-4285-9f6b-96fd9131628a");
    }

    @Test
    void createNotification_withoutAuth_shouldReturnUnauthorized() {
        var requestBody = """
                {
                    "type": "TRANSFER",
                    "accountId": "afd94176-3179-4285-9f6b-96fd9131628a",
                    "amount": 123.45,
                    "message": "блаблабла"
                }
                """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.postForEntity("/api/notifications", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createNotification_withInvalidData_shouldReturnBadRequest() {
        var requestBody = """
                {
                    "type": null,
                    "accountId": null,
                    "amount": -1,
                    "message": ""
                }
                """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.postForEntity(
                "/api/notifications", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
