package ru.devinvader.bank.notifications.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.service.NotificationService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void send_validRequest_shouldSaveToDatabase() {
        var countBefore = notificationRepository.count();
        var accountId = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var request = new NotificationRequest(NotificationType.TRANSFER, accountId,
                new BigDecimal("100.50"), "перевели 100 500 монеток");

        var result = notificationService.send(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);

        var countAfter = notificationRepository.count();
        assertThat(countAfter).isEqualTo(countBefore + 1);

        var found = notificationRepository.findById(result.id());
        assertThat(found).isPresent();
        assertThat(found.get().accountId()).isEqualTo(accountId);
        assertThat(found.get().status()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_validRequest_shouldPersistAllFields() {
        var accountId = UUID.fromString("bfd94176-3179-4285-9f6b-96fd9131628b");
        var request = new NotificationRequest(NotificationType.DEPOSIT, accountId,
                new BigDecimal("250.00"), "депозит");

        var result = notificationService.send(request);

        assertThat(result.id()).isNotNull();

        assertThat(notificationRepository.existsById(result.id())).isTrue();

        var saved = notificationRepository.findById(result.id()).orElseThrow();
        assertThat(saved.type()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(saved.accountId()).isEqualTo(accountId);
        assertThat(saved.amount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(saved.message()).isEqualTo("депозит");
        assertThat(saved.createdAt()).isNotNull();
        assertThat(saved.sentAt()).isNotNull();
    }
}
