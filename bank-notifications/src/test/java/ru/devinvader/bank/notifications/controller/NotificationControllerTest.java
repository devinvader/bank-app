package ru.devinvader.bank.notifications.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.notifications.config.SecurityConfig;
import ru.devinvader.bank.notifications.config.TestSecurityConfig;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationRequest;
import ru.devinvader.bank.notifications.model.NotificationStatus;
import ru.devinvader.bank.notifications.model.NotificationType;
import ru.devinvader.bank.notifications.service.NotificationService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createNotification_withValidJwtAndBody_shouldReturnOk() throws Exception {
        var notification = new Notification(UUID.randomUUID(), NotificationType.TRANSFER, "user123",
                new BigDecimal("100.50"), "Test transfer", NotificationStatus.SENT, Instant.now(), Instant.now());

        when(notificationService.send(any(NotificationRequest.class))).thenReturn(notification);

        mockMvc.perform(
                        post("/api/notifications")
                                .with(jwt().jwt(jwt -> jwt.claim("scope", "notifications:send")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "type": "TRANSFER",
                                            "accountId": "user123",
                                            "amount": 100.50,
                                            "message": "Test transfer"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }

    @Test
    void createNotification_withWrongScope_shouldReturnForbidden() throws Exception {
        mockMvc.perform(
                post("/api/notifications")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "accounts:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "TRANSFER",
                                    "accountId": "user123",
                                    "amount": 100.50,
                                    "message": "Test"
                                }
                                """)).andExpect(status().isForbidden());
    }

    @Test
    void createNotification_withInvalidBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                post("/api/notifications")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "notifications:send")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": null,
                                    "accountId": "",
                                    "amount": -1,
                                    "message": ""
                                }
                                """)).andExpect(status().isBadRequest());
    }
}
