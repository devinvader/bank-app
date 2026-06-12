package ru.devinvader.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository repository;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        service = new TransferServiceImpl(repository, null, null,
                new NotificationMessages(messageSource), new TransferMapper());
    }

    @Test
    void execute_shouldFailWithoutWebClient() {
        var request = new TransferRequest(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));
        when(repository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.execute(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), request))
                .isInstanceOf(RuntimeException.class);
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
