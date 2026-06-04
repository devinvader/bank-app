package ru.devinvader.bank.notifications.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Table("notifications")
public record Notification(
        @Id UUID id,
        NotificationType type,
        String accountId,
        BigDecimal amount,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt
) {
}
