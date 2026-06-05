package ru.devinvader.bank.transfer.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        String fromLogin,
        String toLogin,
        BigDecimal amount,
        TransferStatus status,
        Instant timestamp
) {}
