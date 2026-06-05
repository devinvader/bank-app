package ru.devinvader.bank.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
@Import({SecurityConfig.class, TestSecurityConfig.class})
@TestPropertySource(properties = {"spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration,org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientServletAutoConfiguration"})
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Test
    void execute_withValidJwt_shouldReturnOk() throws Exception {
        var response = new TransferResponse(UUID.randomUUID(), "user1", "user2",
                BigDecimal.valueOf(100), TransferStatus.COMPLETED, Instant.now());
        when(transferService.execute(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")
                                .claim("preferred_username", "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toLogin": "user2",
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
                                    "toLogin": "user2",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void execute_withInvalidBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:execute")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toLogin": "",
                                    "amount": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_withValidJwt_shouldReturnList() throws Exception {
        var response = new TransferResponse(UUID.randomUUID(), "user1", "user2",
                BigDecimal.valueOf(100), TransferStatus.COMPLETED, Instant.now());
        when(transferService.getHistory("user1")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transfer/history")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "transfer:read")
                                .claim("preferred_username", "user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromLogin").value("user1"));
    }
}
