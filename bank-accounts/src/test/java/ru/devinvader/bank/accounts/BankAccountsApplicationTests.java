package ru.devinvader.bank.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.accounts.integration.TestcontainersConfiguration;

@SpringBootTest
@Import({TestSecurityConfig.class, TestcontainersConfiguration.class})
class BankAccountsApplicationTests {

    @Test
    void contextLoads() {
    }

}
