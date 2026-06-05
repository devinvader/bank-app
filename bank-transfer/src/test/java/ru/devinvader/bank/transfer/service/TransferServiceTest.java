package ru.devinvader.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        service = new TransferServiceImpl(repository, null);
    }

    @Test
    void execute_shouldFailWithoutWebClient() {
        var request = new TransferRequest("user2", BigDecimal.valueOf(100));
        when(repository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.execute("user1", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getHistory_shouldReturnTransfers() {
        var transfer = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount("user1")
                .toAccount("user2")
                .amount(BigDecimal.valueOf(100))
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        when(repository.findByFromAccount("user1")).thenReturn(List.of(transfer));

        var history = service.getHistory("user1");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).fromLogin()).isEqualTo("user1");
    }
}
