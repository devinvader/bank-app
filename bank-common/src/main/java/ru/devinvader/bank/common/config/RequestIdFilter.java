package ru.devinvader.bank.common.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.devinvader.bank.common.util.RequestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    public static final String SPAN_ID_ATTRIBUTE = RequestConstants.SPAN_ID_ATTRIBUTE;
    public static final String TRACE_ID_ATTRIBUTE = RequestConstants.TRACE_ID_ATTRIBUTE;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var traceId = request.getHeader(RequestConstants.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        var spanId = UUID.randomUUID().toString();

        request.setAttribute(RequestConstants.SPAN_ID_ATTRIBUTE, spanId);
        request.setAttribute(RequestConstants.TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(RequestConstants.SPAN_ID_HEADER, spanId);
        response.setHeader(RequestConstants.TRACE_ID_HEADER, traceId);

        var hasAuth = request.getHeader("Authorization") != null;
        log.info(">>> {} {} auth={} trace={}", request.getMethod(), request.getRequestURI(), hasAuth, traceId);

        try {
            MDC.put(RequestConstants.MDC_SPAN_ID, spanId);
            MDC.put(RequestConstants.MDC_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            var status = response.getStatus();
            if (status >= 400) {
                log.warn("<<< {} {} status={} trace={}", request.getMethod(), request.getRequestURI(), status, traceId);
            } else {
                log.info("<<< {} {} status={} trace={}", request.getMethod(), request.getRequestURI(), status, traceId);
            }
            MDC.remove(RequestConstants.MDC_SPAN_ID);
            MDC.remove(RequestConstants.MDC_TRACE_ID);
        }
    }
}
