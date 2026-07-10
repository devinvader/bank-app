package ru.devinvader.bank.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.accounts.exception.AccountNotFoundException;
import ru.devinvader.bank.accounts.exception.AgeValidationException;
import ru.devinvader.bank.accounts.exception.InsufficientBalanceException;
import ru.devinvader.bank.accounts.mapper.AccountMapper;
import ru.devinvader.bank.accounts.model.Account;
import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;
import ru.devinvader.bank.accounts.repository.AccountRepository;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final NotificationClient notificationClient;
    private final NotificationMessages notificationMessages;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse getCurrentOrCreate(UUID currentUserId) {
        var account = repository.findById(currentUserId)
                .orElseGet(() -> createDefaultAccount(currentUserId));
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getById(UUID accountId) {
        var account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return accountMapper.toResponse(account);
    }

    @Override
    public List<UUID> findMissingAccounts(Collection<UUID> accountIds) {
        var existing = new HashSet<>(repository.findExistingIds(accountIds));
        return accountIds.stream()
                .filter(id -> !existing.contains(id))
                .distinct()
                .toList();
    }

    @Override
    public List<AccountResponse> getTransferTargets(UUID excludeAccountId) {        return repository.findAllByIdNot(excludeAccountId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AccountResponse update(UUID accountId, AccountRequest request) {
        validateAge(request.birthdate());
        var account = repository.findById(accountId)
                .orElseGet(() -> createDefaultAccount(accountId));
        account = account.toBuilder()
                .name(request.name())
                .birthdate(request.birthdate())
                .updatedAt(Instant.now())
                .build();
        var result = accountMapper.toResponse(repository.save(account));
        try {
            notificationClient.send(NotificationType.PROFILE_UPDATE, accountId, BigDecimal.ZERO,
                    notificationMessages.forProfileUpdate(accountId));
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public void debit(UUID accountId, BigDecimal amount) {
        var account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        int updated = repository.debit(accountId, amount, Instant.now());
        if (updated == 0) {
            throw new InsufficientBalanceException(amount, account.balance());
        }
        notificationClient.send(NotificationType.WITHDRAWAL, accountId, amount,
                notificationMessages.forWithdrawal(accountId, amount));
    }

    @Override
    @Transactional
    public void credit(UUID accountId, BigDecimal amount) {
        int updated = repository.credit(accountId, amount, Instant.now());
        if (updated == 0) {
            throw new AccountNotFoundException(accountId);
        }
        notificationClient.send(NotificationType.DEPOSIT, accountId, amount,
                notificationMessages.forDeposit(accountId, amount));
    }

    private void validateAge(LocalDate birthdate) {
        if (Period.between(birthdate, LocalDate.now()).getYears() < 18) {
            throw new AgeValidationException("User must be at least 18 years old");
        }
    }

    private Account createDefaultAccount(UUID accountId) {
        var account = Account.builder()
                .id(accountId)
                .name("Новый пользователь")
                .birthdate(LocalDate.of(2000, 1, 1))
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .newEntity(true)
                .build();
        log.info("Auto-created account for new user: {}", accountId);
        return repository.save(account);
    }
}
