package ru.devinvader.bank.accounts.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.accounts.model.Account;
import ru.devinvader.bank.accounts.model.AccountResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        var id = UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a");
        var account = Account.builder()
                .id(id)
                .name("Test User")
                .birthdate(LocalDate.of(1990, 6, 15))
                .balance(BigDecimal.valueOf(1500))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .newEntity(false)
                .build();

        AccountResponse response = mapper.toResponse(account);

        assertThat(response.accountId()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.birthdate()).isEqualTo(LocalDate.of(1990, 6, 15));
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(1500));
    }

    @Test
    void toResponse_withZeroBalance_shouldMapCorrectly() {
        var id = UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a");
        var account = Account.builder()
                .id(id)
                .name("Zero")
                .birthdate(LocalDate.of(2000, 1, 1))
                .balance(BigDecimal.ZERO)
                .build();

        AccountResponse response = mapper.toResponse(account);

        assertThat(response.balance()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.accountId()).isEqualTo(id);
    }
}
