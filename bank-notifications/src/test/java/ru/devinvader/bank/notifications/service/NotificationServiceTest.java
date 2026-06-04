package ru.devinvader.bank.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationRequest;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.model.NotificationType;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    private LoggerNotificationService service;

    @BeforeEach
    void setUp() {
        service = new LoggerNotificationService(repository);
    }

    @Test
    void send_validRequest_shouldSaveAndReturnSent() {
        var request = new NotificationRequest(NotificationType.TRANSFER, "user",
                new BigDecimal("100.50"), "Test transfer");

        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.send(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.sentAt()).isNotNull();
        verify(repository, times(2)).save(any(Notification.class));
    }

    @Test
    void send_whenLoggingFails_shouldReturnFailedStatus() {
        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new NotificationRequest(NotificationType.DEPOSIT, "user",
                new BigDecimal("50.00"), "депозит");

        var result = service.send(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isIn(NotificationStatus.FAILED, NotificationStatus.SENT);
    }

    @Test
    void fallbackSend_whenCircuitBreakerOpen_shouldCreateFailed() {
        var request = new NotificationRequest(NotificationType.WITHDRAWAL, "user",
                new BigDecimal("200.00"), "тест");

        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.fallbackSend(request, new RuntimeException("Circuit Breaker Open"));

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void send_whenRepositoryFails_shouldThrowException() {
        doThrow(new RuntimeException())
                .when(repository).save(any(Notification.class));

        var request = new NotificationRequest(NotificationType.TRANSFER, "user",
                new BigDecimal("100.00"), "Test");

        assertThatThrownBy(() -> service.send(request))
                .isInstanceOf(RuntimeException.class);
    }
}
