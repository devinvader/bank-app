package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashFrontServiceImpl implements CashFrontService {
    private final BankApiClient bankApiClient;
    private final AccountFrontService accountFrontService;
    private final TokenProvider tokenProvider;

    @Override
    public AccountPageModel processCashOperation(BigDecimal amount, CashAction action) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return getCurrentPage().withErrors(List.of("Сумма должна быть положительной"));

        try {
            var accountId = UUID.fromString(tokenProvider.getUsername());
            if (action == CashAction.DEPOSIT) {
                bankApiClient.deposit(amount);
                return getCurrentPage().withInfo("Положено " + amount + " руб");
            } else {
                bankApiClient.withdraw(amount);
                return getCurrentPage().withInfo("Снято " + amount + " руб");
            }
        } catch (InsufficientFundsException e) {
            return getCurrentPage().withErrors(List.of(e.getMessage()));
        } catch (ServiceUnavailableException e) {
            return getCurrentPage().withErrors(List.of("Сервис временно недоступен"));
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cash operation failed", e);
            return getCurrentPage().withErrors(List.of("Ошибка операции"));
        }
    }

    private AccountPageModel getCurrentPage() { return accountFrontService.getAccountPage(); }
}
