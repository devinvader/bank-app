package ru.devinvader.bank.common.util;

public final class RequestConstants {

    private RequestConstants() {}

    public static final String SPAN_ID_HEADER = "X-Span-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String SPAN_ID_ATTRIBUTE = "spanId";
    public static final String TRACE_ID_ATTRIBUTE = "traceId";
}
