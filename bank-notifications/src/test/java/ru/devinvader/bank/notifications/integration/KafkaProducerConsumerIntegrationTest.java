package ru.devinvader.bank.notifications.integration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.devinvader.bank.common.config.NotificationDeserializer;
import ru.devinvader.bank.common.config.NotificationSerializer;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class KafkaProducerConsumerIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    private KafkaProducer<String, NotificationRequest> producer;
    private KafkaConsumer<String, NotificationRequest> consumer;

    @BeforeEach
    void setUp() {
        var producerProps = Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationSerializer.class
        );
        producer = new KafkaProducer<>(producerProps);

        var consumerProps = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        consumer = new KafkaConsumer<>(consumerProps);
    }

    @AfterEach
    void tearDown() {
        producer.close();
        consumer.close();
    }

    @Test
    void produceAndConsume_shouldReceiveExactMessage() {
        var accountId = UUID.randomUUID();
        var request = new NotificationRequest(NotificationType.DEPOSIT, accountId,
                BigDecimal.valueOf(500), "Test message");

        producer.send(new ProducerRecord<>("test-topic", accountId.toString(), request));
        producer.flush();

        consumer.subscribe(List.of("test-topic"));
        var records = consumer.poll(Duration.ofSeconds(10));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, NotificationRequest> record = records.iterator().next();
        assertThat(record.value().type()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(record.value().accountId()).isEqualTo(accountId);
        assertThat(record.value().amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(record.value().message()).isEqualTo("Test message");
    }

    @Test
    void produceTransferNotification_shouldReceiveWithCorrectType() {
        var accountId = UUID.randomUUID();
        var request = new NotificationRequest(NotificationType.TRANSFER, accountId,
                BigDecimal.valueOf(1000), "Transfer message");

        producer.send(new ProducerRecord<>("test-topic-2", accountId.toString(), request));
        producer.flush();

        consumer.subscribe(List.of("test-topic-2"));
        var records = consumer.poll(Duration.ofSeconds(10));

        assertThat(records.count()).isEqualTo(1);
        var record = records.iterator().next();
        assertThat(record.value().type()).isEqualTo(NotificationType.TRANSFER);
        assertThat(record.value().amount()).isEqualTo(BigDecimal.valueOf(1000));
    }
}
