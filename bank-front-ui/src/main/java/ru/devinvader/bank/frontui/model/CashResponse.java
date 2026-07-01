package ru.devinvader.bank.frontui.model;

import java.math.BigDecimal;
import java.util.UUID;

public record CashResponse(UUID accountId, BigDecimal newBalance, String type, BigDecimal amount) {}
