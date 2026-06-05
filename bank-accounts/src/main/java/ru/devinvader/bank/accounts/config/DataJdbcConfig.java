package ru.devinvader.bank.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import ru.devinvader.bank.accounts.model.Account;

import java.util.UUID;

@Configuration
public class DataJdbcConfig {

    @Bean
    public BeforeConvertCallback<Account> accountIdGenerator() {
        return account -> {
            if (account.id() == null) {
                return account.toBuilder().id(UUID.randomUUID()).build();
            }
            return account;
        };
    }
}
