package ru.devinvader.bank.cash.model;

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
@Table("cash_operations")
public record CashOperation(
        @Id UUID id,
        UUID accountId,
        CashOperationType type,
        BigDecimal amount,
        Instant createdAt,
        @Transient Boolean newEntity
) implements Persistable<UUID> {

    @PersistenceCreator
    public CashOperation(UUID id, UUID accountId, CashOperationType type,
                         BigDecimal amount, Instant createdAt) {
        this(id, accountId, type, amount, createdAt, null);
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
