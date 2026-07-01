package ru.devinvader.bank.notifications.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationStatus;

import java.time.Instant;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request) {
        return Notification.builder()
                .type(request.type())
                .accountId(request.accountId())
                .amount(request.amount())
                .message(request.message())
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .newEntity(true)
                .build();
    }
}
