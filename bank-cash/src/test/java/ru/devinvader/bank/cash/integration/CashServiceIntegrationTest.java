package ru.devinvader.bank.cash.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CashServiceIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cashRepository.deleteAll();
    }

    @Test
    void saveAndFind_shouldPersistOperation() {
        var operation = CashOperation.builder()
                .accountId(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))
                .type(CashOperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        cashRepository.save(operation);

        var operations = cashRepository.findByAccountId(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"));
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0).type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(operations.get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void findByAccountId_shouldReturnMultipleOperations() {
        var op1 = CashOperation.builder()
                .accountId(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))
                .type(CashOperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        var op2 = CashOperation.builder()
                .accountId(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))
                .type(CashOperationType.WITHDRAWAL)
                .amount(BigDecimal.valueOf(30))
                .createdAt(Instant.now())
                .build();
        cashRepository.save(op1);
        cashRepository.save(op2);

        var operations = cashRepository.findByAccountId(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"));
        assertThat(operations).hasSize(2);
    }

    @Test
    void findByAccountId_shouldReturnEmptyForUnknownUser() {
        var operations = cashRepository.findByAccountId(UUID.randomUUID());
        assertThat(operations).isEmpty();
    }
}
