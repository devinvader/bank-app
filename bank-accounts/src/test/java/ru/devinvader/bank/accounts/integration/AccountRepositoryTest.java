package ru.devinvader.bank.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devinvader.bank.accounts.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void saveAndFindById_shouldPersist() {
        var id = UUID.randomUUID();
        var account = Account.builder()
                .id(id)
                .name("Test User")
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .newEntity(true)
                .build();

        accountRepository.save(account);

        var found = accountRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
    }

    @Test
    void findById_notFound_shouldReturnEmpty() {
        var found = accountRepository.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByIdNot_shouldExclude() {
        var id1 = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var id2 = UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a");
        accountRepository.save(account(id1, "User One"));
        accountRepository.save(account(id2, "User Two"));

        var result = accountRepository.findAllByIdNot(id1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(id2);
    }

    @Test
    void update_shouldPersistChanges() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        accountRepository.save(account(id, "Old Name"));

        var found = accountRepository.findById(id).orElseThrow();
        var updated = found.toBuilder()
                .name("New Name")
                .updatedAt(Instant.now())
                .build();
        accountRepository.save(updated);

        var refetched = accountRepository.findById(id);
        assertThat(refetched).isPresent();
        assertThat(refetched.get().name()).isEqualTo("New Name");
    }

    private static Account account(UUID id, String name) {
        return Account.builder()
                .id(id)
                .name(name)
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .newEntity(true)
                .build();
    }
}
