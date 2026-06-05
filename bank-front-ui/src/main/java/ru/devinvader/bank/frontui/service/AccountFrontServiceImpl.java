package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountFrontServiceImpl implements AccountFrontService {
    private final BankApiClient bankApiClient;

    @Override
    public AccountPageModel getAccountPage() {
        try {
            var account = bankApiClient.getAccount();
            var targets = bankApiClient.getTransferTargets();
            return new AccountPageModel(account.name(), account.birthdate(),
                    account.balance(), targets, null, null);
        } catch (UnauthorizedException e) {
            return AccountPageModel.withError("Требуется повторная аутентификация");
        } catch (ServiceUnavailableException e) {
            return AccountPageModel.withError("Сервис временно недоступен");
        } catch (Exception e) {
            log.error("Failed to load account page", e);
            return AccountPageModel.withError("Ошибка загрузки данных");
        }
    }

    @Override
    public AccountPageModel updateAccount(String name, LocalDate birthdate) {
        if (Period.between(birthdate, LocalDate.now()).getYears() < 18)
            return AccountPageModel.withError("Возраст должен быть не менее 18 лет");
        try {
            bankApiClient.updateAccount(new AccountUpdateRequestDto(name, birthdate));
            return getAccountPage().withInfo("Данные сохранены");
        } catch (BadRequestException e) {
            return getAccountPage().withErrors(List.of("Некорректные данные: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            return AccountPageModel.withError("Требуется повторная аутентификация");
        } catch (ServiceUnavailableException e) {
            return getAccountPage().withErrors(List.of("Сервис временно недоступен"));
        } catch (Exception e) {
            log.error("Failed to update account", e);
            return getAccountPage().withErrors(List.of("Ошибка сохранения"));
        }
    }
}
