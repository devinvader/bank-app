package ru.devinvader.bank.common.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.devinvader.bank.common.mapper.NotificationRequestMapper;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

    @Mock
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(any(), any(), any(NotificationRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        notificationClient = new NotificationClient(kafkaTemplate,
                new NotificationRequestMapper());
        setField(notificationClient, "topic", "notifications-test");
    }

    @Test
    void send_depositRequest_shouldSendToKafka() {
        var accountId = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");

        assertThatCode(() -> notificationClient.send(NotificationType.CASH_DEPOSIT,
                accountId, BigDecimal.valueOf(100), "Test deposit message"))
                .doesNotThrowAnyException();

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(kafkaTemplate).send(eq("notifications-test"), eq(accountId.toString()), captor.capture());

        var sent = captor.getValue();
        assertThat(sent.type()).isEqualTo(NotificationType.CASH_DEPOSIT);
        assertThat(sent.accountId()).isEqualTo(accountId);
        assertThat(sent.amount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(sent.message()).contains("Test deposit message");
    }

    @Test
    void send_transferRequest_shouldSendWithCorrectType() {
        var accountId = UUID.randomUUID();

        notificationClient.send(NotificationType.TRANSFER_SENT, accountId,
                BigDecimal.valueOf(500), "Transfer message");

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(kafkaTemplate).send(eq("notifications-test"), eq(accountId.toString()), captor.capture());

        var sent = captor.getValue();
        assertThat(sent.type()).isEqualTo(NotificationType.TRANSFER_SENT);
        assertThat(sent.amount()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void send_withdrawalRequest_shouldSendWithCorrectType() {
        var accountId = UUID.randomUUID();

        notificationClient.send(NotificationType.CASH_WITHDRAWAL, accountId,
                BigDecimal.valueOf(200), "Withdrawal message");

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(kafkaTemplate).send(eq("notifications-test"), eq(accountId.toString()), captor.capture());

        var sent = captor.getValue();
        assertThat(sent.type()).isEqualTo(NotificationType.CASH_WITHDRAWAL);
    }

    @Test
    void send_profileUpdateRequest_shouldSendWithZeroAmount() {
        var accountId = UUID.randomUUID();

        notificationClient.send(NotificationType.PROFILE_UPDATE, accountId,
                BigDecimal.ZERO, "Profile updated");

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(kafkaTemplate).send(eq("notifications-test"), eq(accountId.toString()), captor.capture());

        var sent = captor.getValue();
        assertThat(sent.type()).isEqualTo(NotificationType.PROFILE_UPDATE);
        assertThat(sent.amount()).isEqualTo(BigDecimal.ZERO);
    }
}
