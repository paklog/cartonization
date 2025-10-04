package com.paklog.cartonization.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor that adds correlation IDs to requests for distributed tracing.
 * Extracts correlation ID from headers or generates a new one if not present.
 */
@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdInterceptor.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extract or generate correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing correlation ID: {}", correlationId);
        }

        // Extract or generate request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
            log.debug("Generated new request ID: {}", requestId);
        } else {
            log.debug("Using existing request ID: {}", requestId);
        }

        // Add to MDC for logging
        MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        // Add to response headers for tracing
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Store in request attributes for access in controllers
        request.setAttribute(MDC_CORRELATION_ID_KEY, correlationId);
        request.setAttribute(MDC_REQUEST_ID_KEY, requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Clean up MDC to prevent memory leaks
        MDC.remove(MDC_CORRELATION_ID_KEY);
        MDC.remove(MDC_REQUEST_ID_KEY);
    }

    /**
     * Gets the current correlation ID from MDC.
     *
     * @return the correlation ID, or null if not set
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID_KEY);
    }

    /**
     * Gets the current request ID from MDC.
     *
     * @return the request ID, or null if not set
     */
    public static String getCurrentRequestId() {
        return MDC.get(MDC_REQUEST_ID_KEY);
    }
}
