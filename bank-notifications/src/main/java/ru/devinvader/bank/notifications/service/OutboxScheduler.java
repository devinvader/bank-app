package ru.devinvader.bank.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final NotificationRepository repository;

    @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
    @Transactional
    public void processOutbox() {
        var failedNotifications = repository.findByStatus(NotificationStatus.FAILED);

        for (Notification notification : failedNotifications) {
            try {
                log.info("Retrying notification: {}", notification.id());
                notification = notification.toBuilder()
                        .status(NotificationStatus.SENT)
                        .sentAt(Instant.now())
                        .build();
                repository.save(notification);
            } catch (Exception e) {
                log.error("Retry failed for notification: {}", notification.id(), e);
            }
        }
    }
}
