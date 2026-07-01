package ru.devinvader.bank.frontui.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.BadRequestException;
import ru.devinvader.bank.frontui.exception.InsufficientFundsException;
import ru.devinvader.bank.frontui.exception.ServiceUnavailableException;
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.AccountDto;
import ru.devinvader.bank.frontui.model.AccountPageModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferFrontServiceTest {

    @Mock
    private BankApiClient bankApiClient;

    @Mock
    private AccountFrontService accountFrontService;

    @Spy
    private FrontUiMapper frontUiMapper;

    @InjectMocks
    private TransferFrontServiceImpl transferFrontService;

    private AccountPageModel mockCurrentPage() {
        var page = new AccountPageModel("Тест", LocalDate.of(1990, 1, 1),
                BigDecimal.valueOf(1000), List.of(new AccountDto(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), "Петр")), null, null);
        when(accountFrontService.getAccountPage()).thenReturn(page);
        return page;
    }

    @Test
    void transfer_validData_shouldReturnSuccess() {
        mockCurrentPage();

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));

        assertThat(page.info()).contains("Успешно переведено");
        assertThat(page.info()).contains("Петр");
    }

    @Test
    void transfer_negativeAmount_shouldReturnValidationError() {
        mockCurrentPage();

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(-50));

        assertThat(page.errors()).contains("Сумма перевода должна быть положительной");
    }

    @Test
    void transfer_zeroAmount_shouldReturnValidationError() {
        mockCurrentPage();

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.ZERO);

        assertThat(page.errors()).contains("Сумма перевода должна быть положительной");
    }

    @Test
    void transfer_nullTarget_shouldReturnValidationError() {
        mockCurrentPage();

        var page = transferFrontService.processTransfer(null, BigDecimal.TEN);

        assertThat(page.errors()).contains("Выберите получателя");
    }

    @Test
    void transfer_insufficientFunds_shouldReturnError() {
        mockCurrentPage();
        doThrow(new InsufficientFundsException("Недостаточно средств"))
                .when(bankApiClient).transfer(any());

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(99999));

        assertThat(page.errors()).contains("Недостаточно средств");
    }

    @Test
    void transfer_badRequest_shouldReturnError() {
        mockCurrentPage();
        doThrow(new BadRequestException("Bad request"))
                .when(bankApiClient).transfer(any());

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));

        assertThat(page.errors()).contains("Некорректные данные перевода");
    }

    @Test
    void transfer_serviceUnavailable_shouldReturnError() {
        mockCurrentPage();
        doThrow(new ServiceUnavailableException("Service down"))
                .when(bankApiClient).transfer(any());

        var page = transferFrontService.processTransfer(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), BigDecimal.valueOf(100));

        assertThat(page.errors()).contains("Сервис временно недоступен");
    }
}
