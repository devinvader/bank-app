package ru.devinvader.bank.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devinvader.bank.accounts.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void saveAndFindByLogin_shouldPersist() {
        var account = Account.builder()
                .id(null)
                .login("testuser")
                .name("Test User")
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        accountRepository.save(account);

        var found = accountRepository.findByLogin("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().login()).isEqualTo("testuser");
    }

    @Test
    void findByLogin_notFound_shouldReturnEmpty() {
        var found = accountRepository.findByLogin("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByLoginNot_shouldExclude() {
        accountRepository.save(account("user1", "User One"));
        accountRepository.save(account("user2", "User Two"));

        var result = accountRepository.findAllByLoginNot("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("user2");
    }

    @Test
    void update_shouldPersistChanges() {
        accountRepository.save(account("user1", "Old Name"));

        var found = accountRepository.findByLogin("user1").orElseThrow();
        var updated = found.toBuilder()
                .name("New Name")
                .updatedAt(Instant.now())
                .build();
        accountRepository.save(updated);

        var refetched = accountRepository.findByLogin("user1");
        assertThat(refetched).isPresent();
        assertThat(refetched.get().name()).isEqualTo("New Name");
    }

    private static Account account(String login, String name) {
        return Account.builder()
                .id(null)
                .login(login)
                .name(name)
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
