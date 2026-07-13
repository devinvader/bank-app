package ru.devinvader.bank.notifications.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import ru.devinvader.bank.common.config.NotificationDeserializer;
import ru.devinvader.bank.common.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@EnableConfigurationProperties(NotificationConsumerProperties.class)
public class KafkaConsumerConfig {

    private final NotificationConsumerProperties properties;

    @Value("${spring.kafka.bootstrap-servers:${spring.embedded.kafka.brokers:}}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, NotificationRequest> notificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationRequest>
    notificationKafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationRequest> notificationConsumerFactory,
            DefaultErrorHandler defaultErrorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, NotificationRequest>();
        factory.setConsumerFactory(notificationConsumerFactory);
        factory.setAutoStartup(properties.isAutoStartup());
        factory.setCommonErrorHandler(defaultErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate) {
        return new DeadLetterPublishingRecoverer(notificationKafkaTemplate,
                (cr, e) -> new TopicPartition(
                        cr.topic() + properties.getDltSuffix(), cr.partition()));
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        var errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer,
                new FixedBackOff(properties.getRetryIntervalMs(), properties.getRetryMaxAttempts()));
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.error("Retry attempt {} for notification: key={}, topic={}, partition={}, offset={}, error={}",
                        deliveryAttempt, record.key(), record.topic(),
                        record.partition(), record.offset(), ex.getMessage()));
        return errorHandler;
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.kafka.bootstrap-servers:}')")
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
