package ru.devinvader.bank.cash.contracts;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class AccountsConsumerContractTest extends CashControllerBase {

    @Test
    void shouldGetAccountViaContract() {
        URL stubUrl = stubFinder.findStubUrl("bank-accounts");
        var restTemplate = new RestTemplate();

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            stubUrl + "/api/accounts/me",
            org.springframework.http.HttpMethod.GET,
            entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("afd94176-3179-4285-9f6b-96fd9131628a");
    }

    @Test
    void shouldGetAccountByLoginViaContract() {
        URL stubUrl = stubFinder.findStubUrl("bank-accounts");
        var restTemplate = new RestTemplate();

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            stubUrl + "/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a",
            org.springframework.http.HttpMethod.GET,
            entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("afd94176-3179-4285-9f6b-96fd9131628a");
    }

    @Test
    void shouldDebitAccountViaContract() {
        URL stubUrl = stubFinder.findStubUrl("bank-accounts");
        var restTemplate = new RestTemplate();

        var requestBody = """
            {
                "amount": 100
            }
            """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            stubUrl + "/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a/debit", entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void shouldCreditAccountViaContract() {
        URL stubUrl = stubFinder.findStubUrl("bank-accounts");
        var restTemplate = new RestTemplate();

        var requestBody = """
            {
                "amount": 100
            }
            """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        var entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            stubUrl + "/api/accounts/afd94176-3179-4285-9f6b-96fd9131628a/credit", entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
