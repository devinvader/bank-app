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
import ru.devinvader.bank.frontui.exception.*;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountFrontServiceTest {

    @Mock
    private BankApiClient bankApiClient;

    @InjectMocks
    private AccountFrontServiceImpl accountFrontService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(new DefaultOAuth2User(List.of(), Map.of("sub", "user1"), "sub"),
                        List.of(), "keycloak"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccountPage_success_shouldReturnPage() {
        var response = new AccountResponse("user1", "Иван Иванов",
                LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));
        when(bankApiClient.getAccount()).thenReturn(response);
        when(bankApiClient.getTransferTargets()).thenReturn(
                List.of(new AccountDto("user2", "Петр Петров")));

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
    void getAccountPage_unauthorized_shouldReturnError() {
        when(bankApiClient.getAccount()).thenThrow(new UnauthorizedException("unauthorized"));

        var page = accountFrontService.getAccountPage();

        assertThat(page.errors()).contains("Требуется повторная аутентификация");
    }

    @Test
    void updateAccount_validData_shouldReturnSuccess() {
        when(bankApiClient.getAccount()).thenReturn(
                new AccountResponse("user1", "Иван Иванов", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000)));
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
                new AccountResponse("user1", "Иван", LocalDate.of(1990, 1, 1), BigDecimal.ZERO));
        when(bankApiClient.getTransferTargets()).thenReturn(List.of());

        var page = accountFrontService.updateAccount("Иван", LocalDate.of(1990, 1, 1));

        assertThat(page.errors()).satisfies(errors -> {
            assertThat(errors).anyMatch(e -> e.contains("Некорректные данные"));
        });
    }
}
