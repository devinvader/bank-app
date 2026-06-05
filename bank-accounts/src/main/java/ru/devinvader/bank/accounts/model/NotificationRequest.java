package ru.devinvader.bank.accounts.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record NotificationRequest(
        @NotBlank String type,
        @NotBlank String accountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String message
) {}
