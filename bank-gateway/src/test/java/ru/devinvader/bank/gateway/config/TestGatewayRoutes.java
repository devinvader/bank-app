package ru.devinvader.bank.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestGatewayRoutes {

    // порт должен не использоваться
    private static final String DOWNSTREAM = "http://localhost:54321";

    @Bean
    public RouteLocator testRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("accounts", r -> r.path("/api/accounts/**")
                        .uri(DOWNSTREAM))
                .route("cash", r -> r.path("/api/cash/**")
                        .uri(DOWNSTREAM))
                .route("transfer", r -> r.path("/api/transfer/**")
                        .uri(DOWNSTREAM))
                .route("notifications", r -> r.path("/api/notifications/**")
                        .uri(DOWNSTREAM))
                .build();
    }
}
