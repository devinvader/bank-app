package ru.devinvader.bank.cash.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.cash.config.TestSecurityConfig;
import ru.devinvader.bank.cash.repository.CashRepository;
import ru.devinvader.bank.commontest.BankIntegrationTest;

@BankIntegrationTest
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected CashRepository cashRepository;
}
