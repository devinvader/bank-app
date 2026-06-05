package ru.devinvader.bank.transfer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${notifications.base-url:lb://bank-notifications}")
    private String notificationsBaseUrl;

    @Override
    @Transactional
    @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackExecute")
    public TransferResponse execute(String fromLogin, TransferRequest request) {
        var transfer = TransferRecord.builder()
                .id(UUID.randomUUID())
                .fromAccount(fromLogin)
                .toAccount(request.toLogin())
                .amount(request.amount())
                .status(TransferStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        transfer = repository.save(transfer);

        try {
            callDebit(fromLogin, request.amount());
            callCredit(request.toLogin(), request.amount());

            transfer = transfer.toBuilder()
                    .status(TransferStatus.COMPLETED)
                    .completedAt(Instant.now())
                    .build();
            transfer = repository.save(transfer);

            sendNotification(fromLogin, "TRANSFER", request.amount(),
                    "Перевод пользователю " + request.toLogin());
            sendNotification(request.toLogin(), "TRANSFER", request.amount(),
                    "Получено от пользователя " + fromLogin);

            return new TransferResponse(transfer.id(), fromLogin, request.toLogin(),
                    request.amount(), TransferStatus.COMPLETED, Instant.now());
        } catch (Exception e) {
            log.error("Transfer failed, attempting compensation: {}", e.getMessage());
            try {
                callCredit(fromLogin, request.amount());
            } catch (Exception compEx) {
                log.error("Compensation failed: {}", compEx.getMessage());
            }

            transfer = transfer.toBuilder()
                    .status(TransferStatus.FAILED)
                    .completedAt(Instant.now())
                    .build();
            repository.save(transfer);

            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    public TransferResponse fallbackExecute(String fromLogin, TransferRequest request, Throwable t) {
        log.error("Fallback execute for transfer from {}: {}", fromLogin, t.getMessage());
        throw new RuntimeException("Transfer service temporarily unavailable");
    }

    @Override
    @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackGetHistory")
    public List<TransferResponse> getHistory(String login) {
        return repository.findByFromAccount(login).stream()
                .map(t -> new TransferResponse(t.id(), t.fromAccount(), t.toAccount(),
                        t.amount(), t.status(), t.createdAt()))
                .toList();
    }

    public List<TransferResponse> fallbackGetHistory(String login, Throwable t) {
        log.error("Fallback getHistory for {}: {}", login, t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackCallDebit")
    private void callDebit(String login, BigDecimal amount) {
        webClientBuilder.build()
                .post()
                .uri("lb://bank-accounts/api/accounts/{login}/debit", login)
                .bodyValue(new BalancePayload(amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void fallbackCallDebit(String login, BigDecimal amount, Throwable t) {
        log.error("Fallback debit for {}: {}", login, t.getMessage());
        throw new RuntimeException("Accounts service unavailable");
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackCallCredit")
    private void callCredit(String login, BigDecimal amount) {
        webClientBuilder.build()
                .post()
                .uri("lb://bank-accounts/api/accounts/{login}/credit", login)
                .bodyValue(new BalancePayload(amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void fallbackCallCredit(String login, BigDecimal amount, Throwable t) {
        log.error("Fallback credit for {}: {}", login, t.getMessage());
        throw new RuntimeException("Accounts service unavailable");
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendNotification")
    private void sendNotification(String login, String type, BigDecimal amount, String message) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationsBaseUrl + "/api/notifications")
                    .bodyValue(new NotificationPayload(type, login, amount, message))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Notification sent for {}: {}", login, message);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    private void fallbackSendNotification(String login, String type, BigDecimal amount,
                                           String message, Throwable t) {
        log.warn("Fallback notification for {}: {}", login, t.getMessage());
    }

    private record BalancePayload(BigDecimal amount) {}

    private record NotificationPayload(String type, String accountId, BigDecimal amount, String message) {}
}
