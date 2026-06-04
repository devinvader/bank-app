package ru.devinvader.bank.notifications.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String accountId,
        @Positive BigDecimal amount,
        @NotBlank String message
) {
}
