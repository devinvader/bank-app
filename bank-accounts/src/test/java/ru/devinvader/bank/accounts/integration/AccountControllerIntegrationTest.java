package ru.devinvader.bank.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class AccountControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void getMe_withoutToken_shouldReturnUnauthorized() {
        var response = restTemplate.getForEntity("/api/accounts/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void debit_withoutToken_shouldReturnUnauthorized() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = "{\"amount\": 100}";
        var entity = new HttpEntity<>(body, headers);

        var response = restTemplate.exchange(
                "/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a/debit", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void credit_withoutToken_shouldReturnUnauthorized() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = "{\"amount\": 100}";
        var entity = new HttpEntity<>(body, headers);

        var response = restTemplate.exchange(
                "/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a/credit", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateMe_withoutToken_shouldReturnUnauthorized() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = "{\"name\": \"Test\", \"birthdate\": \"1990-01-01\"}";
        var entity = new HttpEntity<>(body, headers);

        var response = restTemplate.exchange(
                "/api/accounts/me", HttpMethod.PUT, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
