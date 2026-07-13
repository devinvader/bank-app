package ru.devinvader.bank.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devinvader.bank.accounts.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryConcurrencyTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void concurrentDebit_shouldMaintainConsistency() throws Exception {
        UUID accountId = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = Account.builder()
                .id(accountId)
                .name("Test User")
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .newEntity(true)
                .build();
        accountRepository.save(account);

        var threads = 5;
        var amountPerThread = BigDecimal.valueOf(300);
        var successfulDebits = new AtomicInteger(0);
        var latch = new CountDownLatch(threads);
        try (var executor = Executors.newFixedThreadPool(threads)) {

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        if (accountRepository.debit(accountId, amountPerThread, Instant.now()) > 0) {
                            successfulDebits.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();
        }

        var finalAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(successfulDebits.get()).isEqualTo(3);
        assertThat(finalAccount.balance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }
}
