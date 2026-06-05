package ru.devinvader.bank.accounts.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("accounts")
public record Account(
        @Id UUID id,
        String login,
        String name,
        LocalDate birthdate,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {}
