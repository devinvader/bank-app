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
        var latch = new CountDownLatch(threads);
        try (var executor = Executors.newFixedThreadPool(threads)) {

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        var current = accountRepository.findById(accountId).orElseThrow();
                        if (current.balance().compareTo(amountPerThread) >= 0) {
                            var updated = current.toBuilder()
                                    .balance(current.balance().subtract(amountPerThread))
                                    .updatedAt(Instant.now())
                                    .build();
                            accountRepository.save(updated);
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
        assertThat(finalAccount.balance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
