package ru.devinvader.bank.common.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class NotificationRequestMapper {

    public NotificationRequest toRequest(NotificationType type, UUID accountId,
                                         BigDecimal amount, String message) {
        return new NotificationRequest(type, accountId, amount, message);
    }
}
