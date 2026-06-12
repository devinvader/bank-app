package ru.devinvader.bank.frontui.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.frontui.config.SecurityConfig;
import ru.devinvader.bank.frontui.config.TestSecurityConfig;
import ru.devinvader.bank.frontui.model.AccountDto;
import ru.devinvader.bank.frontui.model.AccountPageModel;
import ru.devinvader.bank.frontui.model.CashAction;
import ru.devinvader.bank.frontui.service.AccountFrontService;
import ru.devinvader.bank.frontui.service.CashFrontService;
import ru.devinvader.bank.frontui.service.TransferFrontService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountFrontService accountFrontService;

    @MockitoBean
    private CashFrontService cashFrontService;

    @MockitoBean
    private TransferFrontService transferFrontService;

    @Test
    void getAccount_shouldReturnMainPage() throws Exception {
        var page = new AccountPageModel("Иван Иванов", LocalDate.of(1990, 1, 1),
                BigDecimal.valueOf(1000), List.of(new AccountDto(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"), "Петр")), null, null);
        when(accountFrontService.getAccountPage()).thenReturn(page);

        mockMvc.perform(get("/account")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read accounts:write")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a"))))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "Иван Иванов"));
    }

    @Test
    void getAccount_unauthenticated_shouldRedirect() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void index_shouldRedirectToAccount() throws Exception {
        mockMvc.perform(get("/")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
    }

    @Test
    void editAccount_validData_shouldRenderMain() throws Exception {
        var page = new AccountPageModel("Иван", LocalDate.of(1990, 1, 1),
                BigDecimal.ZERO, List.of(), null, "Данные сохранены");
        when(accountFrontService.updateAccount(eq("Иван"), eq(LocalDate.of(1990, 1, 1))))
                .thenReturn(page);

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("name", "Иван")
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void editAccount_invalidData_shouldRenderMain() throws Exception {
        var page = new AccountPageModel("", null, BigDecimal.ZERO, List.of(),
                List.of("Возраст должен быть не менее 18 лет"), null);
        when(accountFrontService.updateAccount(eq("Юный"), eq(LocalDate.now().minusYears(17))))
                .thenReturn(page);

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("name", "Юный")
                        .param("birthdate", LocalDate.now().minusYears(17).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void editAccount_serviceUnavailable_shouldRenderError() throws Exception {
        var page = AccountPageModel.withError("Сервис временно недоступен");
        when(accountFrontService.updateAccount(any(), any())).thenReturn(page);

        mockMvc.perform(post("/account")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("name", "Иван")
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void editCash_deposit_shouldRenderMain() throws Exception {
        var page = new AccountPageModel("", null, BigDecimal.ZERO, List.of(), null, "Положено 100 руб");
        when(cashFrontService.processCashOperation(eq(BigDecimal.valueOf(100)), eq(CashAction.DEPOSIT)))
                .thenReturn(page);

        mockMvc.perform(post("/cash")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("value", "100")
                        .param("action", "DEPOSIT"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void editCash_withdraw_shouldRenderMain() throws Exception {
        var page = new AccountPageModel("", null, BigDecimal.ZERO, List.of(), null, "Снято 50 руб");
        when(cashFrontService.processCashOperation(eq(BigDecimal.valueOf(50)), eq(CashAction.WITHDRAW)))
                .thenReturn(page);

        mockMvc.perform(post("/cash")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("value", "50")
                        .param("action", "WITHDRAW"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void editCash_insufficientFunds_shouldShowError() throws Exception {
        var page = AccountPageModel.withError("Недостаточно средств на счету");
        when(cashFrontService.processCashOperation(eq(BigDecimal.valueOf(99999)), eq(CashAction.WITHDRAW)))
                .thenReturn(page);

        mockMvc.perform(post("/cash")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("value", "99999")
                        .param("action", "WITHDRAW"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void transfer_validData_shouldRenderMain() throws Exception {
        var page = new AccountPageModel("", null, BigDecimal.ZERO, List.of(), null, "Успешно переведено 50 руб клиенту 447129a6-bf9b-4dcd-9b35-36d192bb525a");
        when(transferFrontService.processTransfer(eq(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a")), eq(BigDecimal.valueOf(50))))
                .thenReturn(page);

        mockMvc.perform(post("/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("value", "50")
                        .param("accountId", "447129a6-bf9b-4dcd-9b35-36d192bb525a"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void transfer_serviceUnavailable_shouldShowError() throws Exception {
        var page = AccountPageModel.withError("Сервис временно недоступен");
        when(transferFrontService.processTransfer(eq(UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a")), eq(BigDecimal.valueOf(50))))
                .thenReturn(page);

        mockMvc.perform(post("/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .param("value", "50")
                        .param("accountId", "447129a6-bf9b-4dcd-9b35-36d192bb525a"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }
}
