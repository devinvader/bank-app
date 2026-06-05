package ru.devinvader.bank.gateway.config;

import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter implements GlobalFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var requestId = request.getHeaders().getOrEmpty(REQUEST_ID_HEADER)
                .stream().findFirst().orElseGet(() -> UUID.randomUUID().toString());
        var traceId = request.getHeaders().getOrEmpty(TRACE_ID_HEADER)
                .stream().findFirst().orElseGet(() -> UUID.randomUUID().toString());

        var mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .header(TRACE_ID_HEADER, traceId)
                .build();

        var response = exchange.getResponse();
        response.getHeaders().add(REQUEST_ID_HEADER, requestId);
        response.getHeaders().add(TRACE_ID_HEADER, traceId);

        try {
            MDC.put(MDC_REQUEST_ID, requestId);
            MDC.put(MDC_TRACE_ID, traceId);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
