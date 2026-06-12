package ru.devinvader.bank.common.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotNull UUID accountId,
        @PositiveOrZero BigDecimal amount,
        @NotBlank String message
) {
}
