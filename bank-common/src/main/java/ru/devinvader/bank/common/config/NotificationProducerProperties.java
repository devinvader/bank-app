package ru.devinvader.bank.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank.kafka.producer")
public class NotificationProducerProperties {

    private String acks = "all";
    private int retries = 3;
    private int maxBlockMs = 5000;
}
