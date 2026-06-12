package ru.devinvader.bank.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        String name,
        LocalDate birthdate,
        BigDecimal balance
) {}
