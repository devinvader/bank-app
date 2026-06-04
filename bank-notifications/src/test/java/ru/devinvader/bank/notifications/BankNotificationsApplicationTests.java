package ru.devinvader.bank.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(ru.devinvader.bank.notifications.config.TestSecurityConfig.class)
class BankNotificationsApplicationTests {
    @Test
    void contextLoads_shouldLoadApplicationContext() {
    }
}
