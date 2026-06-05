package ru.devinvader.bank.cash.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CashControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void deposit_withoutToken_shouldReturn401() {
        var body = "{\"amount\": 100}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(body, headers);

        assertThatThrownBy(() -> restTemplate.postForEntity(
                baseUrl() + "/api/cash/deposit", entity, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode")
                .isEqualTo(org.springframework.http.HttpStatusCode.valueOf(401));
    }

    @Test
    void deposit_withInvalidBody_shouldReturn400() {
        var body = "{\"amount\": -1}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer test-token");
        var entity = new HttpEntity<>(body, headers);

        assertThatThrownBy(() -> restTemplate.postForEntity(
                baseUrl() + "/api/cash/deposit", entity, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode")
                .isEqualTo(org.springframework.http.HttpStatusCode.valueOf(400));
    }

    @Test
    void withdraw_withoutToken_shouldReturn401() {
        var body = "{\"amount\": 100}";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(body, headers);

        assertThatThrownBy(() -> restTemplate.postForEntity(
                baseUrl() + "/api/cash/withdraw", entity, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode")
                .isEqualTo(org.springframework.http.HttpStatusCode.valueOf(401));
    }
}
