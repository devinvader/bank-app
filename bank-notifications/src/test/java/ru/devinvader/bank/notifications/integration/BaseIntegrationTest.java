package ru.devinvader.bank.notifications.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.commontest.BankIntegrationTest;
import ru.devinvader.bank.notifications.config.TestSecurityConfig;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

@BankIntegrationTest
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected NotificationRepository notificationRepository;
}
