package ru.devinvader.bank.cash.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.cash.config.SecurityConfig;
import ru.devinvader.bank.cash.config.TestSecurityConfig;
import ru.devinvader.bank.common.config.CurrentUserWebMvcConfigurer;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashResponse;
import ru.devinvader.bank.cash.service.CashService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class, CurrentUserWebMvcConfigurer.class})
@TestPropertySource(properties = {"spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration,org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientServletAutoConfiguration", "spring.main.allow-bean-definition-overriding=true"})
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashService cashService;

    @Test
    void deposit_withValidJwt_shouldReturnOk() throws Exception {
        var response = new CashResponse(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), BigDecimal.valueOf(1100),
                CashOperationType.DEPOSIT, BigDecimal.valueOf(100));
        when(cashService.deposit(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/cash/deposit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void withdraw_withValidJwt_shouldReturnOk() throws Exception {
        var response = new CashResponse(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), BigDecimal.valueOf(900),
                CashOperationType.WITHDRAWAL, BigDecimal.valueOf(100));
        when(cashService.withdraw(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/cash/withdraw")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));
    }

    @Test
    void deposit_withWrongScope_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/cash/deposit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "wrong:scope")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deposit_withInvalidBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/cash/deposit")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "cash:operate")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
