package ru.devinvader.bank.cash.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("cash_operations")
public record CashOperation(
        @Id UUID id,
        String accountId,
        CashOperationType type,
        BigDecimal amount,
        Instant createdAt
) {}
