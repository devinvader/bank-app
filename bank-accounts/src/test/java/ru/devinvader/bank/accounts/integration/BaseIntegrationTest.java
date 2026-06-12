package ru.devinvader.bank.accounts.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.accounts.repository.AccountRepository;
import ru.devinvader.bank.commontest.BankIntegrationTest;

@BankIntegrationTest
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected AccountRepository accountRepository;
}
