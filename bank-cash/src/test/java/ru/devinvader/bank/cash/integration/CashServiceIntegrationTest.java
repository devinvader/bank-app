package ru.devinvader.bank.cash.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CashServiceIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cashRepository.deleteAll();
    }

    @Test
    void saveAndFind_shouldPersistOperation() {
        var operation = CashOperation.builder()
                .accountId("user1")
                .type(CashOperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        cashRepository.save(operation);

        var operations = cashRepository.findByAccountId("user1");
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0).type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(operations.get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void findByAccountId_shouldReturnMultipleOperations() {
        var op1 = CashOperation.builder()
                .accountId("user1")
                .type(CashOperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        var op2 = CashOperation.builder()
                .accountId("user1")
                .type(CashOperationType.WITHDRAWAL)
                .amount(BigDecimal.valueOf(30))
                .createdAt(Instant.now())
                .build();
        cashRepository.save(op1);
        cashRepository.save(op2);

        var operations = cashRepository.findByAccountId("user1");
        assertThat(operations).hasSize(2);
    }

    @Test
    void findByAccountId_shouldReturnEmptyForUnknownUser() {
        var operations = cashRepository.findByAccountId("nonexistent");
        assertThat(operations).isEmpty();
    }
}
