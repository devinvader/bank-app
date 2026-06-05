package ru.devinvader.bank.cash.model;

import java.math.BigDecimal;

public record CashResponse(
        String accountId,
        BigDecimal newBalance,
        CashOperationType type,
        BigDecimal amount
) {}
