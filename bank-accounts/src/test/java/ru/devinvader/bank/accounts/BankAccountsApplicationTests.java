package ru.devinvader.bank.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;

@SpringBootTest
@Import({TestSecurityConfig.class, AbstractTestcontainersConfiguration.class})
class BankAccountsApplicationTests {

    @Test
    void contextLoads() {
    }

}
