package ru.devinvader.bank.frontui.model;

import java.math.BigDecimal;

public record CashResponse(String accountId, BigDecimal newBalance, String type, BigDecimal amount) {}
