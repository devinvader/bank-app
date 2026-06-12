package ru.devinvader.bank.transfer.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.commontest.BankIntegrationTest;
import ru.devinvader.bank.transfer.config.TestSecurityConfig;
import ru.devinvader.bank.transfer.repository.TransferRepository;

@BankIntegrationTest
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TransferRepository transferRepository;
}
