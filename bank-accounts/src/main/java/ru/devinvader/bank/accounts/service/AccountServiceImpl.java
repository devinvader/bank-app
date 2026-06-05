package ru.devinvader.bank.accounts.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.accounts.client.NotificationClient;
import ru.devinvader.bank.accounts.exception.AccountNotFoundException;
import ru.devinvader.bank.accounts.exception.AgeValidationException;
import ru.devinvader.bank.accounts.exception.InsufficientBalanceException;
import ru.devinvader.bank.accounts.model.Account;
import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;
import ru.devinvader.bank.accounts.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final NotificationClient notificationClient;

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackGetByLogin")
    public AccountResponse getByLogin(String login) {
        var account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        return toResponse(account);
    }

    public AccountResponse fallbackGetByLogin(String login, Throwable t) {
        log.error("Fallback getByLogin for {}: {}", login, t.getMessage());
        throw new RuntimeException("Service temporarily unavailable");
    }

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackGetTransferTargets")
    public List<AccountResponse> getTransferTargets(String excludeLogin) {
        return repository.findAllByLoginNot(excludeLogin).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AccountResponse> fallbackGetTransferTargets(String excludeLogin, Throwable t) {
        log.error("Fallback getTransferTargets for {}: {}", excludeLogin, t.getMessage());
        return List.of();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackUpdate")
    public AccountResponse update(String login, AccountRequest request) {
        validateAge(request.birthdate());
        var account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        account = account.toBuilder()
                .name(request.name())
                .birthdate(request.birthdate())
                .updatedAt(Instant.now())
                .build();
        var result = toResponse(repository.save(account));
        notificationClient.send("PROFILE_UPDATE", login, BigDecimal.ZERO, "Profile updated for " + login);
        return result;
    }

    public AccountResponse fallbackUpdate(String login, AccountRequest request, Throwable t) {
        log.error("Fallback update for {}: {}", login, t.getMessage());
        throw new RuntimeException("Service temporarily unavailable");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackDebit")
    public void debit(String login, BigDecimal amount) {
        var account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        if (account.balance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(amount, account.balance());
        }
        account = account.toBuilder()
                .balance(account.balance().subtract(amount))
                .updatedAt(Instant.now())
                .build();
        repository.save(account);
        notificationClient.send("DEBIT", login, amount, "Debited " + amount + " from " + login);
    }

    public void fallbackDebit(String login, BigDecimal amount, Throwable t) {
        log.error("Fallback debit for {} amount {}: {}", login, amount, t.getMessage());
        throw new RuntimeException("Service temporarily unavailable");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackCredit")
    public void credit(String login, BigDecimal amount) {
        var account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        account = account.toBuilder()
                .balance(account.balance().add(amount))
                .updatedAt(Instant.now())
                .build();
        repository.save(account);
        notificationClient.send("CREDIT", login, amount, "Credited " + amount + " to " + login);
    }

    public void fallbackCredit(String login, BigDecimal amount, Throwable t) {
        log.error("Fallback credit for {} amount {}: {}", login, amount, t.getMessage());
        throw new RuntimeException("Service temporarily unavailable");
    }

    private void validateAge(LocalDate birthdate) {
        if (Period.between(birthdate, LocalDate.now()).getYears() < 18) {
            throw new AgeValidationException("User must be at least 18 years old");
        }
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.login(), account.name(), account.birthdate(), account.balance());
    }
}
