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

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

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
        registry.addInterceptor(new RequestLoggingInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/actuator/**");
        
        registry.addInterceptor(new RequestIdInterceptor())
            .addPathPatterns("/api/**");

        log.info("Request interceptors registered");
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

    private static class RequestIdInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = java.util.UUID.randomUUID().toString();
            }
            
            // Add to MDC for logging
            org.slf4j.MDC.put("requestId", requestId);
            
            // Add to response
            response.setHeader("X-Request-ID", requestId);
            
            // Store in request for later use
            request.setAttribute("requestId", requestId);
            
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            // Clean up MDC
            org.slf4j.MDC.remove("requestId");
        }
    }
}