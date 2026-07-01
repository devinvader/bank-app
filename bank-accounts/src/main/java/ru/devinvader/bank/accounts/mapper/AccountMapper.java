package ru.devinvader.bank.accounts.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.accounts.model.Account;
import ru.devinvader.bank.accounts.model.AccountResponse;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(account.id(), account.name(),
                account.birthdate(), account.balance());
    }
}
