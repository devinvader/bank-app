package ru.devinvader.bank.cash.service;

import lombok.extern.slf4j.Slf4j;
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
public class CashServiceImpl implements CashService {
    private final CashRepository repository;
    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final NotificationMessages notificationMessages;
    private final CashMapper cashMapper;

    public CashServiceImpl(CashRepository repository,
                           AccountsClient accountsClient,
                           NotificationClient notificationClient,
                           NotificationMessages notificationMessages,
                           CashMapper cashMapper) {
        this.repository = repository;
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.notificationMessages = notificationMessages;
        this.cashMapper = cashMapper;
    }

    @Override
    @Transactional
    public CashResponse deposit(UUID accountId, CashRequest request) {
        var operation = cashMapper.toEntity(accountId, request, CashOperationType.DEPOSIT);
        repository.save(operation);

        var balance = updateBalance(accountId, request.amount(), "credit");

        notificationClient.send(NotificationType.CASH_DEPOSIT, accountId, request.amount(),
                notificationMessages.forDeposit(accountId, request.amount()));

        return cashMapper.toResponse(accountId, balance, CashOperationType.DEPOSIT, request.amount());
    }

    @Override
    @Transactional
    public CashResponse withdraw(UUID accountId, CashRequest request) {
        var operation = cashMapper.toEntity(accountId, request, CashOperationType.WITHDRAWAL);
        repository.save(operation);

        var balance = updateBalance(accountId, request.amount(), "debit");

        notificationClient.send(NotificationType.CASH_WITHDRAWAL, accountId, request.amount(),
                notificationMessages.forWithdrawal(accountId, request.amount()));

        return cashMapper.toResponse(accountId, balance, CashOperationType.WITHDRAWAL, request.amount());
    }

    private BigDecimal updateBalance(UUID accountId, BigDecimal amount, String operation) {
        if ("credit".equals(operation)) {
            accountsClient.credit(accountId, amount);
        } else {
            accountsClient.debit(accountId, amount);
        }

        try {
            var accountResponse = accountsClient.getAccount(accountId);
            return accountResponse != null ? accountResponse.balance() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not fetch updated balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
