package ru.devinvader.bank.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devinvader.bank.accounts.client.NotificationClient;
import ru.devinvader.bank.accounts.exception.AccountNotFoundException;
import ru.devinvader.bank.accounts.exception.AgeValidationException;
import ru.devinvader.bank.accounts.exception.InsufficientBalanceException;
import ru.devinvader.bank.accounts.model.Account;
import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private NotificationClient notificationClient;

    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(repository, notificationClient);
    }

    @Test
    void getByLogin_existingLogin_shouldReturnAccount() {
        var account = account("user1", "Иван Иванов", BigDecimal.valueOf(1000));
        when(repository.findByLogin("user1")).thenReturn(Optional.of(account));

        var result = service.getByLogin("user1");

        assertThat(result).isNotNull();
        assertThat(result.login()).isEqualTo("user1");
        assertThat(result.balance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void getByLogin_nonExistingLogin_shouldThrow() {
        when(repository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByLogin("nonexistent"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void debit_sufficientBalance_shouldDecreaseBalance() {
        var account = account("user1", "Иван", BigDecimal.valueOf(1000));
        when(repository.findByLogin("user1")).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.debit("user1", BigDecimal.valueOf(300));
    }

    @Test
    void debit_insufficientBalance_shouldThrow() {
        var account = account("user1", "Иван", BigDecimal.valueOf(100));
        when(repository.findByLogin("user1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.debit("user1", BigDecimal.valueOf(500)))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void credit_shouldIncreaseBalance() {
        var account = account("user1", "Иван", BigDecimal.valueOf(500));
        when(repository.findByLogin("user1")).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.credit("user1", BigDecimal.valueOf(200));
    }

    @Test
    void update_validRequest_shouldUpdateNameAndBirthdate() {
        var account = account("user1", "Старое Имя", BigDecimal.valueOf(100));
        when(repository.findByLogin("user1")).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AccountRequest("Новое Имя", LocalDate.of(1990, 1, 1));
        var result = service.update("user1", request);

        assertThat(result.name()).isEqualTo("Новое Имя");
    }

    @Test
    void update_ageUnder18_shouldThrow() {
        var request = new AccountRequest("Иван", LocalDate.now().minusYears(17));
        assertThatThrownBy(() -> service.update("user1", request))
                .isInstanceOf(AgeValidationException.class);
    }

    @Test
    void getTransferTargets_shouldExcludeCurrentUser() {
        var acc1 = account("user1", "User One", BigDecimal.ZERO);
        var acc2 = account("user2", "User Two", BigDecimal.ZERO);
        when(repository.findAllByLoginNot("user1")).thenReturn(List.of(acc2));

        var result = service.getTransferTargets("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("user2");
    }

    private static Account account(String login, String name, BigDecimal balance) {
        return Account.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name(name)
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(balance)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
