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
        assertThat(result.getFirst().id()).isEqualTo(id2);
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

    @Test
    void debit_sufficientBalance_shouldDecreaseBalanceAndReturnOne() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(1000)));

        var updated = accountRepository.debit(id, BigDecimal.valueOf(300), Instant.now());

        assertThat(updated).isEqualTo(1);
        assertThat(accountRepository.findById(id).orElseThrow().balance())
                .isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void debit_exactBalance_shouldSucceed() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(300)));

        var updated = accountRepository.debit(id, BigDecimal.valueOf(300), Instant.now());

        assertThat(updated).isEqualTo(1);
        assertThat(accountRepository.findById(id).orElseThrow().balance())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void debit_insufficientBalance_shouldNotChangeBalanceAndReturnZero() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(100)));

        var updated = accountRepository.debit(id, BigDecimal.valueOf(300), Instant.now());

        assertThat(updated).isZero();
        assertThat(accountRepository.findById(id).orElseThrow().balance())
                .isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void debit_nonExistingId_shouldReturnZero() {
        var updated = accountRepository.debit(UUID.randomUUID(), BigDecimal.valueOf(100), Instant.now());

        assertThat(updated).isZero();
    }

    @Test
    void debit_shouldUpdateUpdatedAt() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        // postgres не возвращает с точностью до наносекунды, поэтому установлена конкретная строка с 0 секунд.
        var updatedAt = Instant.parse("2026-10-07T20:00:00Z");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(1000)));

        accountRepository.debit(id, BigDecimal.valueOf(300), updatedAt);

        assertThat(accountRepository.findById(id).orElseThrow().updatedAt())
                .isEqualTo(updatedAt);
    }

    @Test
    void credit_shouldIncreaseBalanceAndReturnOne() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(500)));

        var updated = accountRepository.credit(id, BigDecimal.valueOf(200), Instant.now());

        assertThat(updated).isEqualTo(1);
        assertThat(accountRepository.findById(id).orElseThrow().balance())
                .isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void credit_nonExistingId_shouldReturnZero() {
        var updated = accountRepository.credit(UUID.randomUUID(), BigDecimal.valueOf(200), Instant.now());

        assertThat(updated).isZero();
    }

    @Test
    void credit_shouldUpdateUpdatedAt() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        // postgres не возвращает с точностью до наносекунды, поэтому установлена конкретная строка с 0 секунд.
        var updatedAt = Instant.parse("2026-10-07T20:00:00Z");
        accountRepository.save(account(id, "User", BigDecimal.valueOf(500)));

        accountRepository.credit(id, BigDecimal.valueOf(200), updatedAt);

        assertThat(accountRepository.findById(id).orElseThrow().updatedAt())
                .isEqualTo(updatedAt);
    }

    private static Account account(UUID id, String name) {
        return account(id, name, BigDecimal.ZERO);
    }

    private static Account account(UUID id, String name, BigDecimal balance) {
        return Account.builder()
                .id(id)
                .name(name)
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(balance)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .newEntity(true)
                .build();
    }
}
