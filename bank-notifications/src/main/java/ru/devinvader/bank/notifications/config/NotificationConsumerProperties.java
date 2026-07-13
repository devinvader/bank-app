package ru.devinvader.bank.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank.kafka.consumer")
public class NotificationConsumerProperties {

    private String groupId = "notifications-group";
    private String autoOffsetReset = "earliest";
    private boolean autoStartup = true;
    private boolean enableAutoCommit = false;
    private long retryIntervalMs = 1000;
    private long retryMaxAttempts = 3;
    private String dltSuffix = ".errors";
}
