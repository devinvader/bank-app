package ru.devinvader.bank.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.devinvader.bank.cash.mapper.CashMapper;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.exception.AccountNotFoundException;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.AccountResponse;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.repository.CashRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CashServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");

    @Mock
    private CashRepository repository;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private NotificationClient notificationClient;

    private CashServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any(CashOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        service = new CashServiceImpl(repository, accountsClient, notificationClient,
                new NotificationMessages(messageSource), new CashMapper());
    }

    private void stubSuccessfulOperation(BigDecimal newBalance) {
        lenient().doNothing().when(accountsClient).credit(any(UUID.class), any(BigDecimal.class));
        lenient().doNothing().when(accountsClient).debit(any(UUID.class), any(BigDecimal.class));
        when(accountsClient.getAccount(eq(ACCOUNT_ID)))
                .thenReturn(new AccountResponse(ACCOUNT_ID, "Test", LocalDate.of(1990, 1, 1), newBalance));
    }

    @Test
    void deposit_validRequest_shouldSaveOperation() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit(ACCOUNT_ID, request);

        var captor = ArgumentCaptor.forClass(CashOperation.class);
        verify(repository).save(captor.capture());
        var operation = captor.getValue();
        assertThat(operation.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(operation.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(operation.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void deposit_shouldCallAccountsCredit() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit(ACCOUNT_ID, request);

        verify(accountsClient).credit(eq(ACCOUNT_ID), eq(BigDecimal.valueOf(100)));
    }

    @Test
    void deposit_shouldReturnCorrectResponse() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        var response = service.deposit(ACCOUNT_ID, request);

        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(response.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void withdraw_validRequest_shouldSaveOperation() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.withdraw(ACCOUNT_ID, request);

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
        service.withdraw(ACCOUNT_ID, request);

        verify(accountsClient).debit(eq(ACCOUNT_ID), eq(BigDecimal.valueOf(100)));
    }

    @Test
    void withdraw_shouldReturnCorrectResponse() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        var response = service.withdraw(ACCOUNT_ID, request);

        assertThat(response.newBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(response.type()).isEqualTo(CashOperationType.WITHDRAWAL);
    }

    @Test
    void deposit_whenAccountsFails_shouldThrowRuntimeException() {
        doThrow(new RuntimeException("Accounts service error"))
                .when(accountsClient).credit(any(UUID.class), any(BigDecimal.class));

        var request = new CashRequest(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> service.deposit(ACCOUNT_ID, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Accounts service error");
    }

    @Test
    void deposit_whenAccountNotFound_shouldPropagateAccountNotFound() {
        doThrow(new AccountNotFoundException("account not found: " + ACCOUNT_ID))
                .when(accountsClient).credit(any(UUID.class), any(BigDecimal.class));

        var request = new CashRequest(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> service.deposit(ACCOUNT_ID, request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void withdraw_whenAccountNotFound_shouldPropagateAccountNotFound() {
        doThrow(new AccountNotFoundException("account not found: " + ACCOUNT_ID))
                .when(accountsClient).debit(any(UUID.class), any(BigDecimal.class));

        var request = new CashRequest(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> service.withdraw(ACCOUNT_ID, request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deposit_shouldSendNotification() {
        stubSuccessfulOperation(BigDecimal.valueOf(1100));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.deposit(ACCOUNT_ID, request);

        verify(notificationClient).send(any(), any(UUID.class), any(), anyString());
    }

    @Test
    void withdraw_shouldSendNotification() {
        stubSuccessfulOperation(BigDecimal.valueOf(900));

        var request = new CashRequest(BigDecimal.valueOf(100));
        service.withdraw(ACCOUNT_ID, request);

        verify(notificationClient).send(any(), any(UUID.class), any(), anyString());
    }
}
