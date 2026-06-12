package ru.devinvader.bank.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        var method = request.getMethod();
        var path = request.getURI().getPath();
        var hasAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) != null;
        var traceId = request.getHeaders().getFirst("X-Trace-Id");

        log.info(">>> {} {} auth={} trace={}", method, path, hasAuth, traceId);

        return chain.filter(exchange).doFinally(signal -> {
            var status = exchange.getResponse().getStatusCode();
            var code = status != null ? status.value() : 0;
            if (code >= 400) {
                log.warn("<<< {} {} status={} trace={}", method, path, code, traceId);
            } else {
                log.info("<<< {} {} status={} trace={}", method, path, code, traceId);
            }
        });
    }
}
