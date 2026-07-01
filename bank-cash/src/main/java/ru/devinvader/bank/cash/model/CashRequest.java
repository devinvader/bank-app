package ru.devinvader.bank.cash.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CashRequest(
        @NotNull @Positive BigDecimal amount
) {}
