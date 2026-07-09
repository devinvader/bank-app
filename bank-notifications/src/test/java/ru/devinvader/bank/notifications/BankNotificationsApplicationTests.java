package ru.devinvader.bank.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Import(AbstractTestcontainersConfiguration.class)
class BankNotificationsApplicationTests {
    @Test
    void contextLoads_shouldLoadApplicationContext() {
    }
}
