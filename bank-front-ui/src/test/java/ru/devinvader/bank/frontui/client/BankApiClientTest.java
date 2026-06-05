package ru.devinvader.bank.frontui.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;
import ru.devinvader.bank.frontui.service.TokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BankApiClientTest {

    private BankApiClient bankApiClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        var tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("test-token");

        var testBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(testBuilder).build();
        bankApiClient = new BankApiClient(tokenProvider, "localhost:8081", testBuilder);
    }

    @Test
    void getAccount_validResponse_shouldReturnData() {
        server.expect(requestTo("http://localhost:8081/api/accounts/me"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {"login":"user1","name":"Иван","birthdate":"1990-01-01","balance":1000.00}
                        """, MediaType.APPLICATION_JSON));

        var result = bankApiClient.getAccount();

        assertThat(result.name()).isEqualTo("Иван");
        assertThat(result.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.birthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void getAccount_unauthorized_shouldThrowUnauthorizedException() {
        server.expect(requestTo("http://localhost:8081/api/accounts/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> bankApiClient.getAccount())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getAccount_serverError_shouldThrowServiceUnavailable() {
        server.expect(requestTo("http://localhost:8081/api/accounts/me"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> bankApiClient.getAccount())
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void deposit_valid_shouldReturnResponse() {
        server.expect(requestTo("http://localhost:8081/api/cash/deposit"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().json("{\"accountId\":\"user1\",\"amount\":100}"))
                .andRespond(withSuccess("""
                        {"accountId":"user1","newBalance":1100,"type":"DEPOSIT","amount":100}
                        """, MediaType.APPLICATION_JSON));

        var result = bankApiClient.deposit("user1", BigDecimal.valueOf(100));

        assertThat(result.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
    }

    @Test
    void withdraw_insufficient_shouldThrowInsufficientFunds() {
        server.expect(requestTo("http://localhost:8081/api/cash/withdraw"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withBadRequest().body("Insufficient balance").contentType(MediaType.TEXT_PLAIN));

        assertThatThrownBy(() -> bankApiClient.withdraw("user1", BigDecimal.valueOf(99999)))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_valid_shouldReturnResponse() {
        server.expect(requestTo("http://localhost:8081/api/transfer"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"550e8400-e29b-41d4-a716-446655440000",
                         "fromLogin":"user1","toLogin":"user2",
                         "amount":50,"status":"COMPLETED","timestamp":"2025-01-01T00:00:00Z"}
                        """, MediaType.APPLICATION_JSON));

        var result = bankApiClient.transfer(
                new TransferRequestDto("user2", BigDecimal.valueOf(50)));

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.toLogin()).isEqualTo("user2");
    }

    @Test
    void getTransferTargets_valid_shouldReturnList() {
        server.expect(requestTo("http://localhost:8081/api/accounts/transfer-targets"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"login":"user2","name":"Петр","birthdate":"1992-05-10","balance":500}]
                        """, MediaType.APPLICATION_JSON));

        var result = bankApiClient.getTransferTargets();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().login()).isEqualTo("user2");
    }
}
