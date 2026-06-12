package ru.devinvader.bank.transfer.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("transfers")
public record TransferRecord(
        @Id UUID id,
        UUID fromAccount,
        UUID toAccount,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt,
        Instant completedAt,
        int retryCount,
        @Transient Boolean newEntity
) implements Persistable<UUID> {

    @PersistenceCreator
    public TransferRecord(UUID id, UUID fromAccount, UUID toAccount, BigDecimal amount,
                          TransferStatus status, Instant createdAt, Instant completedAt, int retryCount) {
        this(id, fromAccount, toAccount, amount, status, createdAt, completedAt, retryCount, null);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null || Boolean.TRUE.equals(newEntity);
    }
}
