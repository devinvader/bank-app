package ru.devinvader.bank.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devinvader.bank.common.model.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.notifications:notifications}",
            groupId = "${spring.kafka.consumer.group-id:notifications-group}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void consume(NotificationRequest request) {
        try {
            notificationService.send(request);
        } catch (Exception e) {
            log.error("Failed to process notification: type={}, accountId={}, error={}",
                    request.type(), request.accountId(), e.getMessage());
            throw e;
        }
    }
}
