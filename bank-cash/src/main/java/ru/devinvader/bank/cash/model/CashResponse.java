package ru.devinvader.bank.cash.model;

import java.math.BigDecimal;
import java.util.UUID;

public record CashResponse(
        UUID accountId,
        BigDecimal newBalance,
        CashOperationType type,
        BigDecimal amount
) {}
