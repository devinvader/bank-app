package ru.devinvader.bank.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.devinvader.bank.cash.model.AccountResponse;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.repository.CashRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CashServiceTest {

    @Mock
    private CashRepository repository;

    @Mock
    private WebClient.Builder webClientBuilder;

    private CashServiceImpl service;

    private WebClient webClient;
    private WebClient.RequestBodyUriSpec postUriSpec;
    private WebClient.RequestBodySpec postBodySpec;
    private WebClient.RequestHeadersSpec postHeadersSpec;
    private WebClient.ResponseSpec postResponseSpec;
    private WebClient.RequestHeadersUriSpec getUriSpec;
    private WebClient.RequestHeadersSpec getHeadersSpec;
    private WebClient.ResponseSpec getResponseSpec;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        postUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        postBodySpec = mock(WebClient.RequestBodySpec.class);
        postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        postResponseSpec = mock(WebClient.ResponseSpec.class);
        getUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        getResponseSpec = mock(WebClient.ResponseSpec.class);

        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        lenient().when(webClient.post()).thenReturn(postUriSpec);
        lenient().when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        lenient().when(postUriSpec.uri(anyString(), any(Object[].class))).thenReturn(postBodySpec);
        lenient().when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        lenient().when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);

        lenient().when(webClient.get()).thenReturn(getUriSpec);
        lenient().when(getUriSpec.uri(anyString(), any(Object[].class))).thenReturn(getHeadersSpec);
        lenient().when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);

        lenient().when(repository.save(any(CashOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        service = new CashServiceImpl(repository, webClientBuilder);
    }

    private void stubSuccessfulOperation(BigDecimal newBalance) {
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));
        when(getResponseSpec.bodyToMono(AccountResponse.class))
                .thenReturn(Mono.just(new AccountResponse("user1", "Test", LocalDate.of(1990, 1, 1), newBalance)));
    }

    @Test
    void deposit_validRequest_shouldSaveOperation() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit("user1", request);

        var captor = ArgumentCaptor.forClass(CashOperation.class);
        verify(repository).save(captor.capture());
        var operation = captor.getValue();
        assertThat(operation.accountId()).isEqualTo("user1");
        assertThat(operation.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(operation.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void deposit_shouldCallAccountsCredit() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit("user1", request);

        verify(postUriSpec, atLeastOnce()).uri(anyString(), any(Object[].class));
    }

    @Test
    void deposit_shouldReturnCorrectResponse() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        var response = service.deposit("user1", request);

        assertThat(response.accountId()).isEqualTo("user1");
        assertThat(response.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(response.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void withdraw_validRequest_shouldSaveOperation() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.withdraw("user1", request);

        var captor = ArgumentCaptor.forClass(CashOperation.class);
        verify(repository).save(captor.capture());
        var operation = captor.getValue();
        assertThat(operation.type()).isEqualTo(CashOperationType.WITHDRAWAL);
        assertThat(operation.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void withdraw_shouldCallAccountsDebit() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.withdraw("user1", request);

        verify(postUriSpec, atLeastOnce()).uri(anyString(), any(Object[].class));
    }

    @Test
    void withdraw_shouldReturnCorrectResponse() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        var response = service.withdraw("user1", request);

        assertThat(response.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(response.type()).isEqualTo(CashOperationType.WITHDRAWAL);
    }

    @Test
    void deposit_whenAccountsFails_shouldThrowRuntimeException() {
        when(postResponseSpec.toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        var request = new CashRequest(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> service.deposit("user1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update balance");
    }

    @Test
    void deposit_shouldSendNotification() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit("user1", request);

        verify(postUriSpec, atLeastOnce()).uri(contains("notifications"));
    }

    @Test
    void withdraw_shouldSendNotification() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.withdraw("user1", request);

        verify(postUriSpec, atLeastOnce()).uri(contains("notifications"));
    }

    @Test
    void fallbackDeposit_shouldThrowRuntimeException() {
        var request = new CashRequest(BigDecimal.valueOf(100));
        assertThatThrownBy(() -> service.fallbackDeposit("user1", request, new RuntimeException("test")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("temporarily unavailable");
    }

    @Test
    void fallbackUpdateBalance_shouldThrowRuntimeException() {
        assertThatThrownBy(() -> service.fallbackUpdateBalance("user1", BigDecimal.valueOf(100),
                "credit", new RuntimeException("test")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void notificationFailure_shouldNotCrashDeposit() {
        when(postResponseSpec.toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()))
                .thenThrow(new RuntimeException("Notification failed"));
        when(getResponseSpec.bodyToMono(AccountResponse.class))
                .thenReturn(Mono.just(new AccountResponse("user1", "Test", LocalDate.of(1990, 1, 1),
                        BigDecimal.valueOf(1100))));

        var request = new CashRequest(BigDecimal.valueOf(100));
        var response = service.deposit("user1", request);

        assertThat(response.type()).isEqualTo(CashOperationType.DEPOSIT);
    }
}
