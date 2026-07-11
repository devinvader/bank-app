package ru.devinvader.bank.commontest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class AbstractTestcontainersConfiguration {

    private static final EmbeddedKafkaBroker KAFKA_BROKER =
            new EmbeddedKafkaKraftBroker(1, 1, "notifications", "notifications.errors");

    static {
        KAFKA_BROKER.afterPropertiesSet();
        String brokers = KAFKA_BROKER.getBrokersAsString();
        System.setProperty("spring.embedded.kafka.brokers", brokers);
        System.setProperty("spring.kafka.bootstrap-servers", brokers);
        System.setProperty("spring.kafka.listener.auto-startup", "true");
    }

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        return KAFKA_BROKER;
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
    }
}
