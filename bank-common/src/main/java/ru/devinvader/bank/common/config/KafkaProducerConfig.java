package ru.devinvader.bank.common.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.devinvader.bank.common.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(NotificationProducerProperties.class)
public class KafkaProducerConfig {

    private final NotificationProducerProperties properties;

    @Value("${spring.kafka.bootstrap-servers:${spring.embedded.kafka.brokers:}}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, NotificationRequest> notificationProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, properties.getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getRetries());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, properties.getMaxBlockMs());
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate(
            ProducerFactory<String, NotificationRequest> notificationProducerFactory) {
        var template = new KafkaTemplate<>(notificationProducerFactory);
        template.setObservationEnabled(true);
        return template;
    }
}
