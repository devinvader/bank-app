package ru.devinvader.bank.transfer.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("transfers")
public record TransferRecord(
        @Id UUID id,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt,
        Instant completedAt
) {}
