package ru.devinvader.bank.frontui.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDto(
        @NotNull UUID toAccountId,
        @NotNull @Positive BigDecimal amount
) {}
