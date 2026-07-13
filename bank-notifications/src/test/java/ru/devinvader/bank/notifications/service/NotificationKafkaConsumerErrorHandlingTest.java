package ru.devinvader.bank.notifications.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.devinvader.bank.common.config.NotificationDeserializer;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(AbstractTestcontainersConfiguration.class)
class NotificationKafkaConsumerErrorHandlingTest {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration ASYNC_TIMEOUT = Duration.ofSeconds(20);
    private static final String DLT_TOPIC = "notifications.errors";

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void shouldRetryAndEventuallySucceed() throws Exception {
        var accountId = UUID.randomUUID();
        var request = new NotificationRequest(NotificationType.CASH_DEPOSIT, accountId,
                BigDecimal.valueOf(500), "Retry success");

        doThrow(new RuntimeException("Transient error 1"))
                .doThrow(new RuntimeException("Transient error 2"))
                .doThrow(new RuntimeException("Transient error 3"))
                .doReturn(null)
                .when(notificationService).send(any());

        kafkaTemplate.send("notifications", accountId.toString(), request).get();

        await().atMost(ASYNC_TIMEOUT)
                .untilAsserted(() -> verify(notificationService, times(4)).send(any()));
    }

    @Test
    void shouldSendToDltAfterRetriesExhausted() throws Exception {
        var accountId = UUID.randomUUID();
        var key = accountId.toString();
        var request = new NotificationRequest(NotificationType.CASH_WITHDRAWAL, accountId,
                BigDecimal.valueOf(200), "DLT routing");

        doThrow(new RuntimeException("Permanent failure"))
                .when(notificationService).send(any());

        kafkaTemplate.send("notifications", key, request).get();

        await().atMost(ASYNC_TIMEOUT)
                .untilAsserted(() -> verify(notificationService, atLeast(4)).send(any()));
        await().pollDelay(200, MILLISECONDS).until(() -> true);

        ConsumerRecord<String, NotificationRequest> dltRecord = consumeDlt(key);
        assertThat(dltRecord.value().type()).isEqualTo(NotificationType.CASH_WITHDRAWAL);
        assertThat(dltRecord.value().accountId()).isEqualTo(accountId);
        assertThat(dltRecord.value().amount()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(dltRecord.value().message()).isEqualTo("DLT routing");
    }

    @Test
    void shouldSendToDltWithMetadataPreserved() throws Exception {
        var accountId = UUID.randomUUID();
        var key = accountId.toString();
        var request = new NotificationRequest(NotificationType.PROFILE_UPDATE, accountId,
                BigDecimal.ZERO, "Metadata test");

        doThrow(new RuntimeException("Failure"))
                .when(notificationService).send(any());

        kafkaTemplate.send("notifications", key, request).get();

        await().atMost(ASYNC_TIMEOUT)
                .untilAsserted(() -> verify(notificationService, atLeast(4)).send(any()));
        await().pollDelay(200, MILLISECONDS).until(() -> true);

        ConsumerRecord<String, NotificationRequest> dltRecord = consumeDlt(key);
        assertThat(dltRecord.key()).isEqualTo(key);
    }

    private ConsumerRecord<String, NotificationRequest> consumeDlt(String expectedKey) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID(),
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationDeserializer.class.getName(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (var consumer = new KafkaConsumer<String, NotificationRequest>(props)) {
            consumer.subscribe(List.of(DLT_TOPIC));
            consumer.poll(Duration.ofMillis(100));
            consumer.seekToBeginning(consumer.assignment());
            var records = consumer.poll(POLL_TIMEOUT);

            var matching = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> expectedKey.equals(r.key()))
                    .toList();

            assertThat(matching).as("DLT with account %s", expectedKey).hasSize(1);
            return matching.getFirst();
        }
    }
}
