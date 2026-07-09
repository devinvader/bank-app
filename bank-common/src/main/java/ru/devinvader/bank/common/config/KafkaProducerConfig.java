package ru.devinvader.bank.common.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.devinvader.bank.common.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:${spring.embedded.kafka.brokers:}}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, NotificationRequest> notificationProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate(
            ProducerFactory<String, NotificationRequest> notificationProducerFactory) {
        return new KafkaTemplate<>(notificationProducerFactory);
    }
}
