package ru.devinvader.bank.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestGatewayRoutes.class})
class SecurityConfigTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void health_shouldReturnOk_withoutToken() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void accountsEndpoint_shouldReturnUnauthorized_withoutToken() {
        webTestClient.get().uri("/api/accounts/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void cashEndpoint_shouldReturnUnauthorized_withoutToken() {
        webTestClient.post().uri("/api/cash/deposit")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void transferEndpoint_shouldReturnUnauthorized_withoutToken() {
        webTestClient.post().uri("/api/transfer")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void notificationsEndpoint_shouldReturnUnauthorized_withoutToken() {
        webTestClient.post().uri("/api/notifications")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
