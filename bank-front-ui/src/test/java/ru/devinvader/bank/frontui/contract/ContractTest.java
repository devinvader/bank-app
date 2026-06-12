package ru.devinvader.bank.frontui.contract;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.AccountUpdateRequestDto;
import ru.devinvader.bank.frontui.model.TransferRequestDto;
import ru.devinvader.bank.frontui.service.TokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractTest {

    private static WireMockServer wireMockServer;
    private static BankApiClient bankApiClient;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        var baseUrl = "localhost:" + wireMockServer.port();
        var factory = new SimpleClientHttpRequestFactory();

        var tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("test-token");

        bankApiClient = new BankApiClient(tokenProvider, baseUrl, RestClient.builder().requestFactory(factory), new FrontUiMapper());
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getAccount_shouldReturnData() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/accounts/me"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"accountId":"afd94176-3179-4285-9f6b-96fd9131628a","name":"Иван","birthdate":"1990-01-01","balance":1000.00}
                                """)));

        var result = bankApiClient.getAccount();

        assertThat(result.name()).isEqualTo("Иван");
    }

    @Test
    void updateAccount_shouldReturnData() {
        wireMockServer.stubFor(put(urlPathEqualTo("/api/accounts/me"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"accountId":"afd94176-3179-4285-9f6b-96fd9131628a","name":"Иван","birthdate":"1990-01-01","balance":1000.00}
                                """)));

        var result = bankApiClient.updateAccount(
                new AccountUpdateRequestDto("Иван", LocalDate.of(1990, 1, 1)));

        assertThat(result.name()).isEqualTo("Иван");
    }

    @Test
    void deposit_shouldReturnData() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/cash/deposit"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"accountId":"afd94176-3179-4285-9f6b-96fd9131628a","newBalance":1100.00,"type":"DEPOSIT","amount":100.00}
                                """)));

        var result = bankApiClient.deposit(BigDecimal.valueOf(100));

        assertThat(result.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
    }

    @Test
    void withdraw_shouldReturnData() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/cash/withdraw"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"accountId":"afd94176-3179-4285-9f6b-96fd9131628a","newBalance":900.00,"type":"WITHDRAWAL","amount":100.00}
                                """)));

        var result = bankApiClient.withdraw(BigDecimal.valueOf(100));

        assertThat(result.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
    }

    @Test
    void transfer_shouldReturnData() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/transfer"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"550e8400-e29b-41d4-a716-446655440000","fromAccountId":"afd94176-3179-4285-9f6b-96fd9131628a",
                                 "toAccountId":"447129a6-bf9b-4dcd-9b35-36d192bb525a","amount":50.00,"status":"COMPLETED","timestamp":"2025-01-01T00:00:00Z"}
                                """)));

        var result = bankApiClient.transfer(
                new TransferRequestDto(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(50)));

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.toAccountId()).isEqualTo(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"));
    }

    @Test
    void getTransferTargets_shouldReturnList() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/accounts/transfer-targets"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [{"accountId":"447129a6-bf9b-4dcd-9b35-36d192bb525a","name":"Петр","birthdate":"1992-05-10","balance":500.00}]
                                """)));

        var result = bankApiClient.getTransferTargets();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().accountId()).isEqualTo(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"));
    }
}
