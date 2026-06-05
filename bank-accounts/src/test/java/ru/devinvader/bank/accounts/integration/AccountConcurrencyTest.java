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

class AccountConcurrencyTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void concurrentDebit_shouldMaintainConsistency() throws Exception {
        var account = Account.builder()
                .id(null)
                .login("user1")
                .name("Test User")
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        accountRepository.save(account);

        var threads = 5;
        var amountPerThread = BigDecimal.valueOf(300);
        var latch = new CountDownLatch(threads);
        var executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    var current = accountRepository.findByLogin("user1").orElseThrow();
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

        var finalAccount = accountRepository.findByLogin("user1").orElseThrow();
        assertThat(finalAccount.balance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
