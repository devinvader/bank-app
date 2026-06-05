package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferFrontServiceImpl implements TransferFrontService {
    private final BankApiClient bankApiClient;
    private final AccountFrontService accountFrontService;

    @Override
    public AccountPageModel processTransfer(String targetLogin, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return getCurrentPage().withErrors(List.of("Сумма перевода должна быть положительной"));
        if (targetLogin == null || targetLogin.isBlank())
            return getCurrentPage().withErrors(List.of("Выберите получателя"));

        try {
            bankApiClient.transfer(new TransferRequestDto(targetLogin, amount));
            return getCurrentPage().withInfo("Успешно переведено " + amount + " руб клиенту " + targetLogin);
        } catch (InsufficientFundsException e) {
            return getCurrentPage().withErrors(List.of(e.getMessage()));
        } catch (BadRequestException e) {
            return getCurrentPage().withErrors(List.of("Некорректные данные перевода"));
        } catch (ServiceUnavailableException e) {
            return getCurrentPage().withErrors(List.of("Сервис временно недоступен"));
        } catch (Exception e) {
            log.error("Transfer failed", e);
            return getCurrentPage().withErrors(List.of("Ошибка перевода"));
        }
    }

    private AccountPageModel getCurrentPage() { return accountFrontService.getAccountPage(); }
}
