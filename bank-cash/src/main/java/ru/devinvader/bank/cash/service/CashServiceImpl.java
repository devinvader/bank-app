package ru.devinvader.bank.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.cash.mapper.CashMapper;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;
import ru.devinvader.bank.cash.repository.CashRepository;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {
    private final CashRepository repository;
    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final NotificationMessages notificationMessages;
    private final CashMapper cashMapper;

    @Override
    @Transactional
    @Timed(value = "bank.cash.deposit", description = "Пополнение счёта")
    public CashResponse deposit(UUID accountId, CashRequest request) {
        var operation = cashMapper.toEntity(accountId, request, CashOperationType.DEPOSIT);
        repository.save(operation);

        var balance = creditBalance(accountId, request.amount());

        notificationClient.send(NotificationType.CASH_DEPOSIT, accountId, request.amount(),
                notificationMessages.forDeposit(accountId, request.amount()));

        return cashMapper.toResponse(accountId, balance, CashOperationType.DEPOSIT, request.amount());
    }

    @Override
    @Transactional
    @Timed(value = "bank.cash.withdraw", description = "Снятие со счёта")
    @Counted(value = "bank.cash.withdrawals.failed", recordFailuresOnly = true,
            description = "Количество неудачных снятий со счёта")
    public CashResponse withdraw(UUID accountId, CashRequest request) {
        var operation = cashMapper.toEntity(accountId, request, CashOperationType.WITHDRAWAL);
        repository.save(operation);

        var balance = debitBalance(accountId, request.amount());

        notificationClient.send(NotificationType.CASH_WITHDRAWAL, accountId, request.amount(),
                notificationMessages.forWithdrawal(accountId, request.amount()));

        return cashMapper.toResponse(accountId, balance, CashOperationType.WITHDRAWAL, request.amount());
    }

    private BigDecimal creditBalance(UUID accountId, BigDecimal amount) {
        accountsClient.credit(accountId, amount);
        return fetchBalance(accountId);
    }

    private BigDecimal debitBalance(UUID accountId, BigDecimal amount) {
        accountsClient.debit(accountId, amount);
        return fetchBalance(accountId);
    }

    private BigDecimal fetchBalance(UUID accountId) {
        try {
            var accountResponse = accountsClient.getAccount(accountId);
            return accountResponse != null ? accountResponse.balance() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not fetch updated balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
