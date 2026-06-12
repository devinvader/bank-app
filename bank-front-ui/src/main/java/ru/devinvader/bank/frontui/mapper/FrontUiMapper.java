package ru.devinvader.bank.frontui.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class FrontUiMapper {

    public AccountDto toAccountDto(AccountResponse accountResponse) {
        return new AccountDto(accountResponse.accountId(), accountResponse.name());
    }

    public AccountPageModel toAccountPageModel(AccountResponse account,
                                               List<AccountDto> targets) {
        return new AccountPageModel(account.name(), account.birthdate(),
                account.balance(), targets, null, null);
    }

    public AccountUpdateRequestDto toAccountUpdateRequest(String name, LocalDate birthdate) {
        return new AccountUpdateRequestDto(name, birthdate);
    }

    public TransferRequestDto toTransferRequest(UUID toAccountId, BigDecimal amount) {
        return new TransferRequestDto(toAccountId, amount);
    }

    public CashRequestDto toCashRequest(BigDecimal amount) {
        return new CashRequestDto(amount);
    }
}
