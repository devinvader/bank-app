package ru.devinvader.bank.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${outbox.scheduler.max-retries:3}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
    @Transactional
    public void processOutbox() {
        var failedNotifications = repository.findByStatus(NotificationStatus.FAILED);

        for (Notification notification : failedNotifications) {
            if (notification.retryCount() >= maxRetries) {
                log.warn("Notification {} exceeded max retries ({}), giving up",
                        notification.id(), maxRetries);
                continue;
            }
            try {
                log.info("Retrying notification: {} (attempt {}/{})",
                        notification.id(), notification.retryCount() + 1, maxRetries);
                notification = notification.toBuilder()
                        .status(NotificationStatus.SENT)
                        .sentAt(Instant.now())
                        .retryCount(notification.retryCount() + 1)
                        .build();
                repository.save(notification);
            } catch (Exception e) {
                log.error("Retry failed for notification: {}", notification.id(), e);
                notification = notification.toBuilder()
                        .retryCount(notification.retryCount() + 1)
                        .build();
                repository.save(notification);
            }
        }
    }
}
