package ru.devinvader.bank.accounts.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("accounts")
public record Account(
        @Getter @Id UUID id,
        String name,
        LocalDate birthdate,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt,
        @Transient Boolean newEntity
) implements Persistable<UUID> {

    @PersistenceCreator
    public Account(UUID id, String name, LocalDate birthdate,
                   BigDecimal balance, Instant createdAt, Instant updatedAt) {
        this(id, name, birthdate, balance, createdAt, updatedAt, null);
    }

    @Override
    public boolean isNew() {
        return Boolean.TRUE.equals(newEntity);
    }
}
