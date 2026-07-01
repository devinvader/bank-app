package ru.devinvader.bank.frontui.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountPageModel(
        String name,
        LocalDate birthdate,
        BigDecimal balance,
        List<AccountDto> transferTargets,
        @Nullable List<String> errors,
        @Nullable String info
) {
    public static AccountPageModel empty() {
        return new AccountPageModel("", null, BigDecimal.ZERO, List.of(), List.of(), null);
    }

    public static AccountPageModel withError(String msg) {
        return new AccountPageModel("", null, BigDecimal.ZERO, List.of(), List.of(msg), null);
    }

    public AccountPageModel withErrors(List<String> errs) {
        return new AccountPageModel(name, birthdate, balance, transferTargets, errs, info);
    }

    public AccountPageModel withInfo(String inf) {
        return new AccountPageModel(name, birthdate, balance, transferTargets, errors, inf);
    }
}
