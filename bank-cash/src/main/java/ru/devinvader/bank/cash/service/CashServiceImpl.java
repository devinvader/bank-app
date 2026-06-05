package ru.devinvader.bank.cash.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;
import ru.devinvader.bank.cash.model.AccountResponse;
import ru.devinvader.bank.cash.repository.CashRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {
    private final CashRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${notifications.base-url:lb://bank-notifications}")
    private String notificationsBaseUrl;

    @Override
    @Transactional
    @CircuitBreaker(name = "cashService", fallbackMethod = "fallbackDeposit")
    public CashResponse deposit(String login, CashRequest request) {
        var operation = CashOperation.builder()
                .accountId(login)
                .type(CashOperationType.DEPOSIT)
                .amount(request.amount())
                .createdAt(Instant.now())
                .build();
        repository.save(operation);

        var balance = updateBalance(login, request.amount(), "credit");

        sendNotification(login, CashOperationType.DEPOSIT, request.amount(), "Зачисление средств");

        return new CashResponse(login, balance, CashOperationType.DEPOSIT, request.amount());
    }

    public CashResponse fallbackDeposit(String login, CashRequest request, Throwable t) {
        log.error("Fallback deposit for {} amount {}: {}", login, request.amount(), t.getMessage());
        throw new RuntimeException("Cash service temporarily unavailable");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "cashService", fallbackMethod = "fallbackWithdraw")
    public CashResponse withdraw(String login, CashRequest request) {
        var operation = CashOperation.builder()
                .accountId(login)
                .type(CashOperationType.WITHDRAWAL)
                .amount(request.amount())
                .createdAt(Instant.now())
                .build();
        repository.save(operation);

        var balance = updateBalance(login, request.amount(), "debit");

        sendNotification(login, CashOperationType.WITHDRAWAL, request.amount(), "Снятие средств");

        return new CashResponse(login, balance, CashOperationType.WITHDRAWAL, request.amount());
    }

    public CashResponse fallbackWithdraw(String login, CashRequest request, Throwable t) {
        log.error("Fallback withdraw for {} amount {}: {}", login, request.amount(), t.getMessage());
        throw new RuntimeException("Cash service temporarily unavailable");
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackUpdateBalance")
    BigDecimal updateBalance(String login, BigDecimal amount, String operation) {
        var response = webClientBuilder.build()
                .post()
                .uri("lb://bank-accounts/api/accounts/{login}/{operation}", login, operation)
                .bodyValue(new BalancePayload(amount))
                .retrieve()
                .toBodilessEntity()
                .block();

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update balance in accounts service");
        }

        try {
            var accountResponse = webClientBuilder.build()
                    .get()
                    .uri("lb://bank-accounts/api/accounts/{login}", login)
                    .retrieve()
                    .bodyToMono(AccountResponse.class)
                    .block();
            return accountResponse != null ? accountResponse.balance() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not fetch updated balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    BigDecimal fallbackUpdateBalance(String login, BigDecimal amount, String operation, Throwable t) {
        log.error("Fallback updateBalance for {}: {}", login, t.getMessage());
        throw new RuntimeException("Accounts service unavailable");
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendNotification")
    void sendNotification(String login, CashOperationType type, BigDecimal amount, String message) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationsBaseUrl + "/api/notifications")
                    .bodyValue(new NotificationPayload(
                            type == CashOperationType.DEPOSIT ? "DEPOSIT" : "WITHDRAWAL",
                            login,
                            amount,
                            message
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Notification sent for {}: {}", login, message);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    void fallbackSendNotification(String login, CashOperationType type, BigDecimal amount,
                                     String message, Throwable t) {
        log.warn("Fallback notification for {}: {}", login, t.getMessage());
    }

    private record BalancePayload(BigDecimal amount) {}

    private record NotificationPayload(String type, String accountId, BigDecimal amount, String message) {}
}
