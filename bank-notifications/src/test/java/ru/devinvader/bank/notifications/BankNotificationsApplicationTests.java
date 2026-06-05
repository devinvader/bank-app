package ru.devinvader.bank.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.notifications.config.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
class BankNotificationsApplicationTests {
    @Test
    void contextLoads_shouldLoadApplicationContext() {
    }
}
