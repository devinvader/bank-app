package ru.devinvader.bank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class GatewayRouteConfig {

    @Value("${gateway.routes.accounts-url:http://bank-accounts}")
    private String accountsUrl;

    @Value("${gateway.routes.cash-url:http://bank-cash}")
    private String cashUrl;

    @Value("${gateway.routes.transfer-url:http://bank-transfer}")
    private String transferUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("accounts", r -> r.path("/api/accounts/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/accounts"))
                                .retry(retry -> retry.setRetries(3)
                                        .setStatuses(HttpStatus.BAD_GATEWAY,
                                                HttpStatus.GATEWAY_TIMEOUT,
                                                HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(accountsUrl))
                .route("cash", r -> r.path("/api/cash/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("cashCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/cash"))
                                .retry(retry -> retry.setRetries(3)
                                        .setStatuses(HttpStatus.BAD_GATEWAY,
                                                HttpStatus.GATEWAY_TIMEOUT,
                                                HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(cashUrl))
                .route("transfer", r -> r.path("/api/transfer/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("transferCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/transfer"))
                                .retry(retry -> retry.setRetries(3)
                                        .setStatuses(HttpStatus.BAD_GATEWAY,
                                                HttpStatus.GATEWAY_TIMEOUT,
                                                HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(transferUrl))
                .build();
    }
}
