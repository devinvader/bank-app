package ru.devinvader.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferFrontServiceImpl implements TransferFrontService {
    private final BankApiClient bankApiClient;
    private final AccountFrontService accountFrontService;
    private final FrontUiMapper frontUiMapper;

    @Override
    public AccountPageModel processTransfer(UUID targetAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return getCurrentPage().withErrors(List.of("Сумма перевода должна быть положительной"));
        if (targetAccountId == null)
            return getCurrentPage().withErrors(List.of("Выберите получателя"));

        try {
            bankApiClient.transfer(frontUiMapper.toTransferRequest(targetAccountId, amount));
            var page = getCurrentPage();
            String targetName = page.transferTargets().stream()
                    .filter(a -> a.accountId().equals(targetAccountId))
                    .map(AccountDto::name)
                    .findFirst()
                    .orElse(targetAccountId.toString());
            return page.withInfo("Успешно переведено " + amount + " руб клиенту " + targetName);
        } catch (InsufficientFundsException e) {
            return getCurrentPage().withErrors(List.of(e.getMessage()));
        } catch (BadRequestException e) {
            return getCurrentPage().withErrors(List.of("Некорректные данные перевода"));
        } catch (ServiceUnavailableException e) {
            return getCurrentPage().withErrors(List.of("Сервис временно недоступен"));
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed", e);
            return getCurrentPage().withErrors(List.of("Ошибка перевода"));
        }
    }

    private AccountPageModel getCurrentPage() { return accountFrontService.getAccountPage(); }
}
