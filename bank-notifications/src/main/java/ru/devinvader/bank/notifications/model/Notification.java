package ru.devinvader.bank.notifications.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("notifications")
public record Notification(
        @Id UUID id,
        NotificationType type,
        UUID accountId,
        BigDecimal amount,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt,
        int retryCount,
        @Transient Boolean newEntity
) implements Persistable<UUID> {

    @PersistenceCreator
    public Notification(UUID id, NotificationType type, UUID accountId,
                        BigDecimal amount, String message, NotificationStatus status,
                        Instant createdAt, Instant sentAt, int retryCount) {
        this(id, type, accountId, amount, message, status, createdAt, sentAt, retryCount, null);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null || Boolean.TRUE.equals(newEntity);
    }
}
