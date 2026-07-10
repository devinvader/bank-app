package ru.devinvader.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.exception.InsufficientBalanceException;
import ru.devinvader.bank.common.model.NotificationMessages;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository repository;

    @Mock
    private AccountsClient accountsClient;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        service = new TransferServiceImpl(repository, accountsClient, null,
                new NotificationMessages(messageSource), new TransferMapper());
    }

    @Test
    void execute_shouldFail_whenDebitFails() {
        var request = new TransferRequest(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));
        when(repository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Accounts service temporarily unavailable"))
                .when(accountsClient).debit(any(UUID.class), any(BigDecimal.class));

        assertThatThrownBy(() -> service.execute(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void execute_shouldRejectTransfer_whenInsufficientBalance() {
        var request = new TransferRequest(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));
        when(repository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new InsufficientBalanceException("Недостаточно средств на счету"))
                .when(accountsClient).debit(any(UUID.class), any(BigDecimal.class));

        assertThatThrownBy(() -> service.execute(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), request))
                .isInstanceOf(InsufficientBalanceException.class);

        var captor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(repository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(TransferRecord::status)
                .contains(TransferStatus.REJECTED)
                .doesNotContain(TransferStatus.COMPLETED);
    }

    @Test
    void getHistory_shouldReturnTransfers() {
        var transfer = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))
                .toAccount(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"))
                .amount(BigDecimal.valueOf(100))
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        when(repository.findByFromAccount(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))).thenReturn(List.of(transfer));

        var history = service.getHistory(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"));

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().fromAccountId()).isEqualTo(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"));
    }
}
