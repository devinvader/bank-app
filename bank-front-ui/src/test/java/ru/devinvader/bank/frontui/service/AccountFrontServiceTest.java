package ru.devinvader.bank.frontui.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import ru.devinvader.bank.frontui.client.BankApiClient;
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.mapper.FrontUiMapper;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountFrontServiceTest {

    @Mock
    private BankApiClient bankApiClient;

    @Spy
    private FrontUiMapper frontUiMapper;

    @InjectMocks
    private AccountFrontServiceImpl accountFrontService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(new DefaultOAuth2User(List.of(), Map.of("sub", "afd94176-3179-4285-9f6b-96fd9131628a"), "sub"),
                        List.of(), "keycloak"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccountPage_success_shouldReturnPage() {
        var response = new AccountResponse(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), "Иван Иванов",
                LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));
        when(bankApiClient.getAccount()).thenReturn(response);
        when(bankApiClient.getTransferTargets()).thenReturn(
                List.of(new AccountDto(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), "Петр Петров")));

        var page = accountFrontService.getAccountPage();

        assertThat(page.name()).isEqualTo("Иван Иванов");
        assertThat(page.balance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(page.transferTargets()).hasSize(1);
        assertThat(page.errors()).isNull();
    }

    @Test
    void getAccountPage_serviceUnavailable_shouldReturnError() {
        when(bankApiClient.getAccount()).thenThrow(ServiceUnavailableException.class);

        var page = accountFrontService.getAccountPage();

        assertThat(page.errors()).contains("Сервис временно недоступен");
    }

    @Test
    void getAccountPage_unauthorized_shouldThrow() {
        when(bankApiClient.getAccount()).thenThrow(new UnauthorizedException("unauthorized"));

        assertThatThrownBy(() -> accountFrontService.getAccountPage())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateAccount_validData_shouldReturnSuccess() {
        when(bankApiClient.getAccount()).thenReturn(
                new AccountResponse(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), "Иван Иванов", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000)));
        when(bankApiClient.getTransferTargets()).thenReturn(List.of());

        var page = accountFrontService.updateAccount("Иван Иванов", LocalDate.of(1990, 1, 1));

        assertThat(page.info()).contains("Данные сохранены");
    }

    @Test
    void updateAccount_under18_shouldReturnError() {
        var page = accountFrontService.updateAccount("Юный", LocalDate.now().minusYears(17));

        assertThat(page.errors()).contains("Возраст должен быть не менее 18 лет");
    }

    @Test
    void updateAccount_badRequest_shouldReturnError() {
        when(bankApiClient.updateAccount(any())).thenThrow(new BadRequestException("Invalid data"));
        when(bankApiClient.getAccount()).thenReturn(
                new AccountResponse(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), "Иван", LocalDate.of(1990, 1, 1), BigDecimal.ZERO));
        when(bankApiClient.getTransferTargets()).thenReturn(List.of());

        var page = accountFrontService.updateAccount("Иван", LocalDate.of(1990, 1, 1));

        assertThat(page.errors()).satisfies(errors -> {
            assertThat(errors).anyMatch(e -> e.contains("Некорректные данные"));
        });
    }
}
