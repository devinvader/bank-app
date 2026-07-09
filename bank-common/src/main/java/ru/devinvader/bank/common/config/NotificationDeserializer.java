package ru.devinvader.bank.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import ru.devinvader.bank.common.model.NotificationRequest;

public class NotificationDeserializer implements Deserializer<NotificationRequest> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NotificationRequest deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, NotificationRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize NotificationRequest", e);
        }
    }
}
