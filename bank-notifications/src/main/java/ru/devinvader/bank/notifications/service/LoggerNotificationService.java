package ru.devinvader.bank.notifications.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationRequest;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggerNotificationService implements NotificationService {
    private final NotificationRepository repository;

    @Override
    @Transactional
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSend")
    public Notification send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .type(request.type())
                .accountId(request.accountId())
                .amount(request.amount())
                .message(request.message())
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // оверинжениринг для консоли, но надо по заданию)
        notification = repository.save(notification);

        // т.к. фактически нет возможности что это зафеилится, в try catch не буду оборачивать
        log.info("Notification sent: type={}, accountId={}, amount={}, message={}",
                notification.type(), notification.accountId(),
                notification.amount(), notification.message());
        notification = notification.toBuilder()
                .status(NotificationStatus.SENT)
                .sentAt(Instant.now())
                .build();

        return repository.save(notification);
    }

    public Notification fallbackSend(NotificationRequest request, Throwable t) {
        log.error("Circuit breaker fallback triggered for notification: {}", t.getMessage());
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .type(request.type())
                .accountId(request.accountId())
                .amount(request.amount())
                .message(request.message())
                .status(NotificationStatus.FAILED)
                .createdAt(Instant.now())
                .build();
        return repository.save(notification);
    }
}
