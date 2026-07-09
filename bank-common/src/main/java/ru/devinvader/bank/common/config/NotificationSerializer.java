package ru.devinvader.bank.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import ru.devinvader.bank.common.model.NotificationRequest;

public class NotificationSerializer implements Serializer<NotificationRequest> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, NotificationRequest data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize NotificationRequest", e);
        }
    }
}
