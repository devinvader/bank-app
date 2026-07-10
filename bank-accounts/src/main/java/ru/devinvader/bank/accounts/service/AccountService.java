package ru.devinvader.bank.accounts.service;

import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse getCurrentOrCreate(UUID currentUserId);

    AccountResponse getById(UUID accountId);

    List<AccountResponse> getTransferTargets(UUID excludeAccountId);

    AccountResponse update(UUID accountId, AccountRequest request);

    void debit(UUID accountId, BigDecimal amount);

    void credit(UUID accountId, BigDecimal amount);
}
