package ru.devinvader.bank.transfer.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        TransferStatus status,
        Instant timestamp
) {}
