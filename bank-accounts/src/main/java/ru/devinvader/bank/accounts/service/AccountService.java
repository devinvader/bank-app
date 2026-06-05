package ru.devinvader.bank.accounts.service;

import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponse getByLogin(String login);

    List<AccountResponse> getTransferTargets(String excludeLogin);

    AccountResponse update(String login, AccountRequest request);

    void debit(String login, BigDecimal amount);

    void credit(String login, BigDecimal amount);
}
