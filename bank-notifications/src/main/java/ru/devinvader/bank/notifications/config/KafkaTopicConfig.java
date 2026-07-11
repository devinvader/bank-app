package ru.devinvader.bank.notifications.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    private final KafkaTopicProperties properties;

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.kafka.bootstrap-servers:}')")
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(properties.getName())
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(properties.getRetentionMs()))
                .build();
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.kafka.bootstrap-servers:}')")
    public NewTopic notificationsDltTopic() {
        return TopicBuilder.name(properties.getName() + ".errors")
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(properties.getRetentionMs()))
                .build();
    }
}
