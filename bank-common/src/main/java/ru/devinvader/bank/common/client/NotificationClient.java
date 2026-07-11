package ru.devinvader.bank.common.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devinvader.bank.common.mapper.NotificationRequestMapper;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    private final NotificationRequestMapper notificationRequestMapper;

    @Value("${kafka.topic.notifications.name:notifications}")
    private String topic;

    public void send(NotificationType type, UUID accountId, BigDecimal amount, String message) {
        var request = notificationRequestMapper.toRequest(type, accountId, amount, message);
        kafkaTemplate.send(topic, accountId.toString(), request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to send notification to Kafka: type={}, accountId={}, error={}",
                                type, accountId, ex.getMessage());
                    } else {
                        log.info("Notification sent to Kafka: type={}, accountId={}", type, accountId);
                    }
                });
    }
}
