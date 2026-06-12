package ru.devinvader.bank.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.notifications.mapper.NotificationMapper;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggerNotificationService implements NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification send(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);

        notification = repository.save(notification);

        log.info("Notification sent: type={}, accountId={}, amount={}, message={}",
                notification.type(), notification.accountId(),
                notification.amount(), notification.message());
        notification = notification.toBuilder()
                .status(NotificationStatus.SENT)
                .sentAt(Instant.now())
                .newEntity(false)
                .build();

        return repository.save(notification);
    }
}
