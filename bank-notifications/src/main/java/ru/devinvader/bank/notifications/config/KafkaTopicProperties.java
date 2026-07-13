package ru.devinvader.bank.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.topic.notifications")
public class KafkaTopicProperties {

    private String name = "notifications";
    private int partitions = 3;
    private int replicas = 1;
    private long retentionMs = 604800000L;
}
