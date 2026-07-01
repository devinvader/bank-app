package ru.devinvader.bank.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.common.config.CurrentUserWebMvcConfigurer;
import ru.devinvader.bank.transfer.config.SecurityConfig;
import ru.devinvader.bank.transfer.config.TestSecurityConfig;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.service.TransferService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class, CurrentUserWebMvcConfigurer.class})
@TestPropertySource(properties = {"spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration,org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientServletAutoConfiguration", "spring.main.allow-bean-definition-overriding=true"})
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Test
    void execute_withValidJwt_shouldReturnOk() throws Exception {
        var response = new TransferResponse(UUID.randomUUID(), UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"),
                BigDecimal.valueOf(100), TransferStatus.COMPLETED, Instant.now());
        when(transferService.execute(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toAccountId": "447129a6-bf9b-4dcd-9b35-36d192bb525a",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void execute_withWrongScope_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "wrong:scope")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toAccountId": "447129a6-bf9b-4dcd-9b35-36d192bb525a",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void execute_withInvalidBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toAccountId": null,
                                    "amount": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_withValidJwt_shouldReturnList() throws Exception {
        var response = new TransferResponse(UUID.randomUUID(), UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"), UUID.fromString("447129a6-bf9b-4dcd-9b35-36d192bb525a"),
                BigDecimal.valueOf(100), TransferStatus.COMPLETED, Instant.now());
        when(transferService.getHistory(UUID.fromString("afd94176-3179-4285-9f6b-96fd9131628a"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transfer/history")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:read")
                                .claim("sub", "afd94176-3179-4285-9f6b-96fd9131628a"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromAccountId").value("afd94176-3179-4285-9f6b-96fd9131628a"));
    }
}
