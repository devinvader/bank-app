package ru.devinvader.bank.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devinvader.bank.accounts.mapper.AccountMapper;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.devinvader.bank.accounts.exception.AccountNotFoundException;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.NotificationType;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        service = new AccountServiceImpl(repository, notificationClient,
                new NotificationMessages(messageSource), new AccountMapper());
    }

    @Test
    void getById_existingId_shouldReturnAccount() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = account(id, "Иван Иванов", BigDecimal.valueOf(1000));
        when(repository.findById(id)).thenReturn(Optional.of(account));

        var result = service.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo(id);
        assertThat(result.balance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void getById_nonExistingId_shouldCreateDefault() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Новый пользователь");
        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void debit_sufficientBalance_shouldDecreaseBalance() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = account(id, "Иван", BigDecimal.valueOf(1000));
        when(repository.findById(id)).thenReturn(Optional.of(account));
        when(repository.debit(eq(id), eq(BigDecimal.valueOf(300)), any(Instant.class))).thenReturn(1);

        service.debit(id, BigDecimal.valueOf(300));

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        var amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(repository).debit(idCaptor.capture(), amountCaptor.capture(), any(Instant.class));
        assertThat(idCaptor.getValue()).isEqualTo(id);
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(BigDecimal.valueOf(300));

        verify(notificationClient).send(eq(NotificationType.WITHDRAWAL), eq(id),
                eq(BigDecimal.valueOf(300)), anyString());
    }

    @Test
    void debit_insufficientBalance_shouldThrow() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = account(id, "Иван", BigDecimal.valueOf(100));
        when(repository.findById(id)).thenReturn(Optional.of(account));
        when(repository.debit(eq(id), eq(BigDecimal.valueOf(500)), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> service.debit(id, BigDecimal.valueOf(500)))
                .isInstanceOf(InsufficientBalanceException.class);
        // Неизменность в этом случае баланса будет гарантироваться репозиторием, 
        // см AccountRepostoryTest#debit_insufficientBalance_shouldNotChangeBalanceAndReturnZero
        verify(notificationClient, never()).send(any(), any(), any(), any());
    }

    @Test
    void credit_shouldIncreaseBalance() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        when(repository.credit(eq(id), eq(BigDecimal.valueOf(200)), any(Instant.class))).thenReturn(1);

        service.credit(id, BigDecimal.valueOf(200));

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        var amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(repository).credit(idCaptor.capture(), amountCaptor.capture(), any(Instant.class));
        assertThat(idCaptor.getValue()).isEqualTo(id);
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(BigDecimal.valueOf(200));

        verify(notificationClient).send(eq(NotificationType.DEPOSIT), eq(id),
                eq(BigDecimal.valueOf(200)), anyString());
    }

    @Test
    void credit_accountNotFound_shouldThrow() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        when(repository.credit(eq(id), eq(BigDecimal.valueOf(200)), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> service.credit(id, BigDecimal.valueOf(200)))
                .isInstanceOf(AccountNotFoundException.class);

        verify(notificationClient, never()).send(any(), any(), any(), any());
    }

    @Test
    void update_validRequest_shouldUpdateNameAndBirthdate() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = account(id, "Старое Имя", BigDecimal.valueOf(100));
        when(repository.findById(id)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AccountRequest("Новое Имя", LocalDate.of(1990, 1, 1));
        var result = service.update(id, request);

        assertThat(result.name()).isEqualTo("Новое Имя");

        var accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(accountCaptor.capture());
        var saved = accountCaptor.getValue();
        assertThat(saved.name()).isEqualTo("Новое Имя");
        assertThat(saved.birthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(saved.balance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void update_ageUnder18_shouldThrow() {
        UUID id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var request = new AccountRequest("Иван", LocalDate.now().minusYears(17));
        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(AgeValidationException.class);
    }

    @Test
    void getTransferTargets_shouldExcludeCurrentUser() {
        UUID id1 = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        UUID id2 = UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a");
        var acc2 = account(id2, "User Two", BigDecimal.ZERO);
        when(repository.findAllByIdNot(id1)).thenReturn(List.of(acc2));

        var result = service.getTransferTargets(id1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().accountId()).isEqualTo(id2);
    }

    private static Account account(UUID id, String name, BigDecimal balance) {
        return Account.builder()
                .id(id)
                .name(name)
                .birthdate(LocalDate.of(1990, 1, 1))
                .balance(balance)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
