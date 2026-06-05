package ru.devinvader.bank.gateway.contracts;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.devinvader.bank.gateway.config.TestGatewayRoutes;
import ru.devinvader.bank.gateway.config.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestGatewayRoutes.class, TestSecurityConfig.class})
public abstract class GatewayContractBase {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.webTestClient(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port)
                        .build()
        );
    }
}
