package ru.devinvader.bank.transfer.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID toAccountId,
        @NotNull @Positive BigDecimal amount
) {}
