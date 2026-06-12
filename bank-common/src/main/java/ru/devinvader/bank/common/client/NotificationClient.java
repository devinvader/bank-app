package ru.devinvader.bank.common.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.common.mapper.NotificationRequestMapper;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient.Builder webClientBuilder;
    private final String notificationsBaseUrl;
    private final NotificationRequestMapper notificationRequestMapper;

    public NotificationClient(WebClient.Builder webClientBuilder,
                              @Value("${notifications.base-url:http://bank-notifications}") String notificationsBaseUrl,
                              NotificationRequestMapper notificationRequestMapper) {
        this.webClientBuilder = webClientBuilder;
        this.notificationsBaseUrl = notificationsBaseUrl;
        this.notificationRequestMapper = notificationRequestMapper;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSend")
    public void send(NotificationType type, UUID accountId, BigDecimal amount, String message) {
        var request = notificationRequestMapper.toRequest(type, accountId, amount, message);
        webClientBuilder.build()
                .post()
                .uri(notificationsBaseUrl + "/api/notifications")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("Notification sent: type={}, accountId={}", type, accountId);
    }

    public void fallbackSend(NotificationType type, UUID accountId, BigDecimal amount,
                             String message, Throwable t) {
        log.error("Notification fallback: type={}, accountId={}, amount={}, message={}, error={}",
                type, accountId, amount, message, t.getMessage());
    }
}
