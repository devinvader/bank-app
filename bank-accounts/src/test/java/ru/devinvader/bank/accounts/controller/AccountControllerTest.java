package ru.devinvader.bank.accounts.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.accounts.config.SecurityConfig;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.accounts.exception.AccountNotFoundException;
import ru.devinvader.bank.accounts.exception.AgeValidationException;
import ru.devinvader.bank.accounts.exception.InsufficientBalanceException;
import ru.devinvader.bank.accounts.model.AccountResponse;
import ru.devinvader.bank.accounts.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
@TestPropertySource(properties = {"spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration,org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientServletAutoConfiguration"})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void getCurrentAccount_withValidJwt_shouldReturnOk() throws Exception {
        var response = new AccountResponse("user1", "Иван Иванов", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));
        when(accountService.getByLogin("user1")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read")
                                .claim("preferred_username", "user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void getCurrentAccount_withWrongScope_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "wrong:scope"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCurrentAccount_withValidData_shouldReturnOk() throws Exception {
        var response = new AccountResponse("user1", "Новое Имя", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));
        when(accountService.update(eq("user1"), any())).thenReturn(response);

        mockMvc.perform(put("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("preferred_username", "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Новое Имя",
                                    "birthdate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"));
    }

    @Test
    void updateCurrentAccount_withInvalidBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("preferred_username", "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "birthdate": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransferTargets_withValidJwt_shouldReturnList() throws Exception {
        var targets = List.of(
                new AccountResponse("user2", "Петр Петров", LocalDate.of(1992, 5, 10), BigDecimal.valueOf(500))
        );
        when(accountService.getTransferTargets("user1")).thenReturn(targets);

        mockMvc.perform(get("/api/accounts/transfer-targets")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read")
                                .claim("preferred_username", "user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("user2"));
    }

    @Test
    void debit_withValidJwtAndBody_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/accounts/user1/debit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void credit_withValidJwtAndBody_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/accounts/user1/credit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentAccount_accountNotFound_shouldReturnNotFound() throws Exception {
        when(accountService.getByLogin("user1")).thenThrow(new AccountNotFoundException("user1"));

        mockMvc.perform(get("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read")
                                .claim("preferred_username", "user1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void debit_insufficientBalance_shouldReturnUnprocessableEntity() throws Exception {
        doThrow(new InsufficientBalanceException(BigDecimal.valueOf(500), BigDecimal.valueOf(100)))
                .when(accountService).debit(eq("user1"), any());

        mockMvc.perform(post("/api/accounts/user1/debit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getByLogin_withValidJwt_shouldReturnAccount() throws Exception {
        var response = new AccountResponse("user1", "Иван Иванов", LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));
        when(accountService.getByLogin("user1")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/user1")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void getByLogin_withWrongScope_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/accounts/user1")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "wrong:scope"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByLogin_accountNotFound_shouldReturnNotFound() throws Exception {
        when(accountService.getByLogin("user1")).thenThrow(new AccountNotFoundException("user1"));

        mockMvc.perform(get("/api/accounts/user1")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ageValidation_shouldReturnBadRequest() throws Exception {
        when(accountService.update(eq("user1"), any()))
                .thenThrow(new AgeValidationException("User must be at least 18"));

        mockMvc.perform(put("/api/accounts/me")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:write")
                                .claim("preferred_username", "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Иван",
                                    "birthdate": "2010-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
