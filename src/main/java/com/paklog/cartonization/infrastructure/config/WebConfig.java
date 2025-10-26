package com.paklog.cartonization.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final CorrelationIdInterceptor correlationIdInterceptor;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    public WebConfig(CorrelationIdInterceptor correlationIdInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins.toArray(new String[0]))
            .allowedMethods(allowedMethods.toArray(new String[0]))
            .allowedHeaders(allowedHeaders.toArray(new String[0]))
            .allowCredentials(allowCredentials)
            .maxAge(maxAge);

        log.info("CORS configured - Origins: {}, Methods: {}, Credentials: {}", 
                allowedOrigins, allowedMethods, allowCredentials);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Correlation ID interceptor (includes request ID handling)
        registry.addInterceptor(correlationIdInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/actuator/**");

        // Request logging interceptor
        registry.addInterceptor(new RequestLoggingInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/actuator/**");

        log.info("Request interceptors registered: correlation tracking, request logging");
    }

    private static class RequestLoggingInterceptor implements HandlerInterceptor {
        private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            long startTime = System.currentTimeMillis();
            request.setAttribute("startTime", startTime);
            
            String requestId = request.getHeader("X-Request-ID");
            if (requestId != null) {
                response.setHeader("X-Request-ID", requestId);
            }
            
            log.debug("Incoming request: {} {} from {}", 
                     request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("{} {} - {} ({}ms)", 
                        request.getMethod(), request.getRequestURI(), 
                        response.getStatus(), duration);
            }
            
            if (ex != null) {
                log.error("Request completed with exception", ex);
            }
        }
    }

}