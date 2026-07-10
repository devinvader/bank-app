package ru.devinvader.bank.common.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.devinvader.bank.common.exception.AccountNotFoundException;
import ru.devinvader.bank.common.exception.InsufficientBalanceException;
import ru.devinvader.bank.common.model.AccountResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class AccountsClient {

    private final WebClient.Builder webClientBuilder;
    private final String accountsBaseUrl;

    public AccountsClient(WebClient.Builder webClientBuilder,
                          @Value("${accounts.base-url:http://bank-accounts}") String accountsBaseUrl) {
        this.webClientBuilder = webClientBuilder;
        this.accountsBaseUrl = accountsBaseUrl;
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackDebit")
    public void debit(UUID accountId, BigDecimal amount) {
        webClientBuilder.build()
                .post()
                .uri(accountsBaseUrl + "/api/accounts/{accountId}/debit", accountId)
                .bodyValue(new BalancePayload(amount))
                .retrieve()
                .onStatus(status -> status.value() == 422,
                        (clientResponse) -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new InsufficientBalanceException("Недостаточно средств на счету"))))
                .onStatus(status -> status.value() == 404,
                        (clientResponse) -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AccountNotFoundException("Аккаунт не найден: " + accountId))))
                .toBodilessEntity()
                .block();
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackCredit")
    public void credit(UUID accountId, BigDecimal amount) {
        webClientBuilder.build()
                .post()
                .uri(accountsBaseUrl + "/api/accounts/{accountId}/credit", accountId)
                .bodyValue(new BalancePayload(amount))
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        (clientResponse) -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AccountNotFoundException("Аккаунт не найден: " + accountId))))
                .toBodilessEntity()
                .block();
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackGetAccount")
    public AccountResponse getAccount(UUID accountId) {
        return webClientBuilder.build()
                .get()
                .uri(accountsBaseUrl + "/api/accounts/{accountId}", accountId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        (clientResponse) -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AccountNotFoundException("Аккаунт не найден: " + accountId))))
                .bodyToMono(AccountResponse.class)
                .block();
    }

    public void fallbackDebit(UUID accountId, BigDecimal amount, Throwable t) {
        rethrowIfBusiness(t);
        log.error("Debit fallback: accountId={}, amount={}, error={}", accountId, amount, t.getMessage());
        throw new RuntimeException("Accounts service temporarily unavailable");
    }

    public void fallbackCredit(UUID accountId, BigDecimal amount, Throwable t) {
        rethrowIfBusiness(t);
        log.error("Credit fallback: accountId={}, amount={}, error={}", accountId, amount, t.getMessage());
        throw new RuntimeException("Accounts service temporarily unavailable");
    }

    public AccountResponse fallbackGetAccount(UUID accountId, Throwable t) {
        rethrowIfBusiness(t);
        log.error("GetAccount fallback: accountId={}, error={}", accountId, t.getMessage());
        throw new RuntimeException("Accounts service temporarily unavailable");
    }

    private void rethrowIfBusiness(Throwable t) {
        if (t instanceof AccountNotFoundException || t instanceof InsufficientBalanceException) {
            throw (RuntimeException) t;
        }
    }

    private record BalancePayload(BigDecimal amount) {}
}
