package ru.devinvader.bank.frontui.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.InsufficientFundsException;
import ru.devinvader.bank.frontui.exception.ServiceUnavailableException;
import ru.devinvader.bank.frontui.model.AccountDto;
import ru.devinvader.bank.frontui.model.AccountPageModel;
import ru.devinvader.bank.frontui.model.CashAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashFrontServiceTest {

    @Mock
    private BankApiClient bankApiClient;

    @Mock
    private AccountFrontService accountFrontService;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private CashFrontServiceImpl cashFrontService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(new DefaultOAuth2User(List.of(), Map.of("sub", "afd94176-3179-4285-9f6b-96fd9131628a"), "sub"),
                        List.of(), "keycloak"));
        lenient().when(tokenProvider.getAccessToken()).thenReturn("test-token");
        lenient().when(tokenProvider.getUsername()).thenReturn("afd94176-3179-4285-9f6b-96fd9131628a");

        var currentPage = new AccountPageModel("Тест", LocalDate.of(1990, 1, 1),
                BigDecimal.valueOf(500), List.of(new AccountDto(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), "Петр")), null, null);
        when(accountFrontService.getAccountPage()).thenReturn(currentPage);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deposit_positiveAmount_shouldReturnSuccess() {
        var page = cashFrontService.processCashOperation(BigDecimal.TEN, CashAction.DEPOSIT);

        assertThat(page.info()).contains("Положено");
    }

    @Test
    void withdraw_insufficientFunds_shouldReturnError() {
        doThrow(new InsufficientFundsException("Недостаточно средств на счету"))
                .when(bankApiClient).withdraw(any(UUID.class), any(BigDecimal.class));

        var page = cashFrontService.processCashOperation(BigDecimal.valueOf(99999), CashAction.WITHDRAW);

        assertThat(page.errors()).contains("Недостаточно средств на счету");
    }

    @Test
    void processOperation_negativeAmount_shouldReturnValidationError() {
        var page = cashFrontService.processCashOperation(BigDecimal.valueOf(-100), CashAction.DEPOSIT);

        assertThat(page.errors()).contains("Сумма должна быть положительной");
    }

    @Test
    void processOperation_serviceUnavailable_shouldReturnError() {
        doThrow(new ServiceUnavailableException("Service down"))
                .when(bankApiClient).deposit(any(UUID.class), any(BigDecimal.class));

        var page = cashFrontService.processCashOperation(BigDecimal.TEN, CashAction.DEPOSIT);

        assertThat(page.errors()).contains("Сервис временно недоступен");
    }
}
