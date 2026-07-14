package ru.devinvader.bank.notifications.service;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devinvader.bank.common.model.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.notifications.name:notifications}",
            groupId = "${spring.kafka.consumer.group-id:notifications-group}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    @Timed(value = "bank.notifications.process", description = "Время обработки уведомлений")
    @Counted(value = "bank.notifications.failed", recordFailuresOnly = true,
            description = "Неудачная обработка уведомлений")
    public void consume(ConsumerRecord<String, NotificationRequest> record) {
        log.info("Received notification: key={}, topic={}, partition={}, offset={}, type={}, accountId={}",
                record.key(), record.topic(), record.partition(), record.offset(),
                record.value().type(), record.value().accountId());
        notificationService.send(record.value());
    }
}
