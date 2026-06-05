package ru.devinvader.bank.accounts.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.accounts.model.NotificationRequest;

import java.math.BigDecimal;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient.Builder webClientBuilder;
    private final String notificationsBaseUrl;

    public NotificationClient(WebClient.Builder webClientBuilder,
                              @Value("${notifications.base-url:http://bank-notifications}") String notificationsBaseUrl) {
        this.webClientBuilder = webClientBuilder;
        this.notificationsBaseUrl = notificationsBaseUrl;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSend")
    public void send(String type, String accountId, BigDecimal amount, String message) {
        var request = new NotificationRequest(type, accountId, amount, message);
        webClientBuilder.build()
                .post()
                .uri(notificationsBaseUrl + "/api/notifications")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("Notification sent: type={}, accountId={}", type, accountId);
    }

    public void fallbackSend(String type, String accountId, BigDecimal amount,
                             String message, Throwable t) {
        log.error("Notification fallback: type={}, accountId={}, error={}",
                type, accountId, t.getMessage());
    }
}
