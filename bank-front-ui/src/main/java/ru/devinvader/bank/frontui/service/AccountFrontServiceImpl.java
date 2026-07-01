package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountFrontServiceImpl implements AccountFrontService {
    private final BankApiClient bankApiClient;
    private final FrontUiMapper frontUiMapper;

    @Override
    public AccountPageModel getAccountPage() {
        try {
            var account = bankApiClient.getAccount();
            if (account == null) {
                log.error("Account API returned null response");
                return AccountPageModel.withError("Получен пустой ответ от сервера");
            }
            var targets = bankApiClient.getTransferTargets();
            return frontUiMapper.toAccountPageModel(account, targets);
        } catch (BadRequestException e) {
            log.error("Bad request to accounts API", e);
            return AccountPageModel.withError("Некорректный запрос: " + e.getMessage());
        } catch (ServiceUnavailableException e) {
            return AccountPageModel.withError("Сервис временно недоступен");
        } catch (UnauthorizedException e) {
            throw e;
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
            bankApiClient.updateAccount(frontUiMapper.toAccountUpdateRequest(name, birthdate));
            return getAccountPage().withInfo("Данные сохранены");
        } catch (BadRequestException e) {
            return getAccountPage().withErrors(List.of("Некорректные данные: " + e.getMessage()));
        } catch (ServiceUnavailableException e) {
            return getAccountPage().withErrors(List.of("Сервис временно недоступен"));
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update account", e);
            return getAccountPage().withErrors(List.of("Ошибка сохранения"));
        }
    }
}
