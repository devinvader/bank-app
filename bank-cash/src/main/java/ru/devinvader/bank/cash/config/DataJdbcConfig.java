package ru.devinvader.bank.cash.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import ru.devinvader.bank.cash.model.CashOperation;

import java.util.UUID;

@Configuration
public class DataJdbcConfig {

    @Bean
    public BeforeConvertCallback<CashOperation> cashOperationIdGenerator() {
        return operation -> {
            if (operation.id() == null) {
                return operation.toBuilder().id(UUID.randomUUID()).build();
            }
            return operation;
        };
    }
}
