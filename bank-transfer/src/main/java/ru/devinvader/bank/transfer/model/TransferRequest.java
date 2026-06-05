package ru.devinvader.bank.transfer.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String toLogin,
        @NotNull @Positive BigDecimal amount
) {}
