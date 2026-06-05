package ru.devinvader.bank.accounts.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationClientTest {

    private MockWebServer mockWebServer;
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        var webClientBuilder = WebClient.builder();
        var baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        notificationClient = new NotificationClient(webClientBuilder, baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void send_validRequest_shouldPostToNotifications() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        assertThatCode(() -> notificationClient.send("DEBIT", "user1",
                BigDecimal.valueOf(100), "Test debit"))
                .doesNotThrowAnyException();

        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/notifications");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getBody().readUtf8()).contains("DEBIT");
    }

    @Test
    void send_serverError_shouldPropagateException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> notificationClient.send("CREDIT", "user1",
                BigDecimal.valueOf(200), "Test credit"))
                .isInstanceOf(org.springframework.web.reactive.function.client.WebClientResponseException.class);
    }
}
