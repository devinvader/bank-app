package ru.devinvader.bank.transfer.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransferMapperTest {

    private final TransferMapper mapper = new TransferMapper();

    private static final UUID SOURCE_UUID = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
    private static final UUID TARGET_UUID = UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a");

    @Test
    void toEntity_shouldMapRequestToEntity() {
        var request = new TransferRequest(TARGET_UUID, BigDecimal.valueOf(1000));
        var entity = mapper.toEntity(SOURCE_UUID, request);

        assertThat(entity.fromAccount()).isEqualTo(SOURCE_UUID);
        assertThat(entity.toAccount()).isEqualTo(TARGET_UUID);
        assertThat(entity.amount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(entity.status()).isEqualTo(TransferStatus.PENDING);
        assertThat(entity.retryCount()).isZero();
        assertThat(entity.newEntity()).isTrue();
        assertThat(entity.createdAt()).isNotNull();
    }

    @Test
    void toResponse_shouldMapEntityToResponse() {
        var entity = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(SOURCE_UUID)
                .toAccount(TARGET_UUID)
                .amount(BigDecimal.valueOf(500))
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .retryCount(1)
                .newEntity(false)
                .build();

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.id());
        assertThat(response.fromAccountId()).isEqualTo(SOURCE_UUID);
        assertThat(response.toAccountId()).isEqualTo(TARGET_UUID);
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(response.timestamp()).isEqualTo(entity.createdAt());
    }

    @Test
    void toResponse_withFailedStatus_shouldMapCorrectly() {
        var entity = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(SOURCE_UUID)
                .toAccount(TARGET_UUID)
                .amount(BigDecimal.ONE)
                .status(TransferStatus.FAILED)
                .createdAt(Instant.now())
                .retryCount(3)
                .build();

        var response = mapper.toResponse(entity);

        assertThat(response.status()).isEqualTo(TransferStatus.FAILED);
    }
}
