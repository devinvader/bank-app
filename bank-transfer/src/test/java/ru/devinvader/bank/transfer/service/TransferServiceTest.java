package ru.devinvader.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.exception.AccountNotFoundException;
import ru.devinvader.bank.common.exception.InsufficientBalanceException;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.NotificationType;
import ru.devinvader.bank.transfer.mapper.TransferMapper;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final UUID FROM = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
    private static final UUID TO = UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a");
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private static final TransferRecord COMPENSATION_FAILED = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(FROM)
                .toAccount(TO)
                .amount(AMOUNT)
                .status(TransferStatus.COMPENSATION_FAILED)
                .createdAt(Instant.now())
                .retryCount(1)
                .build();

    @Mock
    private TransferRepository repository;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private NotificationClient notificationClient;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        service = new TransferServiceImpl(repository, accountsClient, notificationClient,
                new NotificationMessages(messageSource), new TransferMapper());
        lenient().when(repository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(accountsClient.findMissingAccounts(any())).thenReturn(List.of());
    }

    private TransferRequest request() {
        return new TransferRequest(TO, AMOUNT);
    }

    private List<TransferStatus> savedStatuses() {
        var captor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream().map(TransferRecord::status).toList();
    }

    @Test
    void execute_success_shouldDebitThenCredit_saveCompleted_sendTwoNotifications() {
        var response = service.execute(FROM, request());

        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);

        InOrder inOrder = inOrder(accountsClient);
        inOrder.verify(accountsClient).debit(FROM, AMOUNT);
        inOrder.verify(accountsClient).credit(TO, AMOUNT);

        assertThat(savedStatuses()).containsExactly(TransferStatus.PENDING, TransferStatus.COMPLETED);

        verify(notificationClient, times(2))
                .send(any(NotificationType.class), any(UUID.class), eq(AMOUNT), any(String.class));
        verify(notificationClient).send(eq(NotificationType.TRANSFER_SENT), eq(FROM), eq(AMOUNT), any(String.class));
        verify(notificationClient).send(eq(NotificationType.TRANSFER_RECEIVED), eq(TO), eq(AMOUNT), any(String.class));
    }

    @Test
    void execute_insufficientBalance_shouldReject_notCreditNorNotify() {
        doThrow(new InsufficientBalanceException("insufficient balance on account"))
                .when(accountsClient).debit(FROM, AMOUNT);

        assertThatThrownBy(() -> service.execute(FROM, request()))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(accountsClient, never()).credit(any(UUID.class), any(BigDecimal.class));
        assertThat(savedStatuses())
                .contains(TransferStatus.REJECTED)
                .doesNotContain(TransferStatus.COMPLETED);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void execute_debitFailsGeneric_shouldSaveFailed_noCompensation() {
        doThrow(new RuntimeException("accounts service temporarily unavailable"))
                .when(accountsClient).debit(FROM, AMOUNT);

        assertThatThrownBy(() -> service.execute(FROM, request()))
                .isInstanceOf(RuntimeException.class);

        verify(accountsClient, never()).credit(any(UUID.class), any(BigDecimal.class));
        assertThat(savedStatuses())
                .contains(TransferStatus.FAILED)
                .doesNotContain(TransferStatus.COMPLETED);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void execute_accountMissing_shouldRejectFast_noDebit() {
        when(accountsClient.findMissingAccounts(List.of(FROM, TO))).thenReturn(List.of(TO));

        assertThatThrownBy(() -> service.execute(FROM, request()))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountsClient, never()).debit(any(UUID.class), any(BigDecimal.class));
        verify(accountsClient, never()).credit(any(UUID.class), any(BigDecimal.class));
        assertThat(savedStatuses())
                .contains(TransferStatus.REJECTED)
                .doesNotContain(TransferStatus.COMPLETED);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void execute_creditFails_shouldCompensate_andSaveFailed() {
        doThrow(new RuntimeException("credit failed")).when(accountsClient).credit(TO, AMOUNT);

        assertThatThrownBy(() -> service.execute(FROM, request()))
                .isInstanceOf(RuntimeException.class);

        InOrder inOrder = inOrder(accountsClient);
        inOrder.verify(accountsClient).debit(FROM, AMOUNT);
        inOrder.verify(accountsClient).credit(TO, AMOUNT);
        inOrder.verify(accountsClient).credit(FROM, AMOUNT);

        assertThat(savedStatuses())
                .contains(TransferStatus.FAILED)
                .doesNotContain(TransferStatus.COMPLETED);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void execute_compensationFails_shouldSaveCompensationFailed() {
        doThrow(new RuntimeException("credit failed")).when(accountsClient).credit(TO, AMOUNT);
        doThrow(new RuntimeException("compensation failed")).when(accountsClient).credit(FROM, AMOUNT);

        assertThatThrownBy(() -> service.execute(FROM, request()))
                .isInstanceOf(RuntimeException.class);

        verify(accountsClient).credit(FROM, AMOUNT);
        assertThat(savedStatuses())
                .contains(TransferStatus.COMPENSATION_FAILED)
                .doesNotContain(TransferStatus.COMPLETED, TransferStatus.FAILED);
    }

    @Test
    void execute_notificationFails_shouldStillComplete() {
        doThrow(new RuntimeException("kafka down"))
                .when(notificationClient).send(any(NotificationType.class), any(UUID.class),
                        any(BigDecimal.class), any(String.class));

        var response = service.execute(FROM, request());

        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(savedStatuses()).contains(TransferStatus.COMPLETED);
        verify(notificationClient, times(2))
                .send(any(NotificationType.class), any(UUID.class), any(BigDecimal.class), any(String.class));
    }

    @Test
    void resumeTransfer_success_shouldCreditWithoutDebit_andComplete() {
        var existing = COMPENSATION_FAILED;

        var response = service.resumeTransfer(existing);

        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        verify(accountsClient, never()).debit(any(UUID.class), any(BigDecimal.class));
        verify(accountsClient).credit(TO, AMOUNT);

        var captor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(repository, times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertThat(saved.get(0).status()).isEqualTo(TransferStatus.PENDING);
        assertThat(saved.get(0).retryCount()).isEqualTo(2);
        assertThat(saved.get(1).status()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void resumeTransfer_creditFailsAgain_compensationFails_shouldStayCompensationFailed() {
        var existing = COMPENSATION_FAILED;
        doThrow(new RuntimeException("credit failed")).when(accountsClient).credit(TO, AMOUNT);
        doThrow(new RuntimeException("compensation failed")).when(accountsClient).credit(FROM, AMOUNT);

        assertThatThrownBy(() -> service.resumeTransfer(existing))
                .isInstanceOf(RuntimeException.class);

        verify(accountsClient, never()).debit(any(UUID.class), any(BigDecimal.class));
        assertThat(savedStatuses())
                .contains(TransferStatus.COMPENSATION_FAILED)
                .doesNotContain(TransferStatus.COMPLETED);
    }

    @Test
    void retryTransfer_success_shouldIncrementRetryCount_andComplete() {
        var existing = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(FROM)
                .toAccount(TO)
                .amount(AMOUNT)
                .status(TransferStatus.FAILED)
                .createdAt(Instant.now())
                .retryCount(1)
                .build();

        var response = service.retryTransfer(existing);

        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);

        var captor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(repository, times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertThat(saved.get(0).status()).isEqualTo(TransferStatus.PENDING);
        assertThat(saved.get(0).retryCount()).isEqualTo(2);
        assertThat(saved.get(1).status()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void getHistory_shouldReturnMappedTransfers() {
        var transfer = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(FROM)
                .toAccount(TO)
                .amount(AMOUNT)
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        when(repository.findByFromAccount(FROM)).thenReturn(List.of(transfer));

        var history = service.getHistory(FROM);

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().fromAccountId()).isEqualTo(FROM);
    }
}
